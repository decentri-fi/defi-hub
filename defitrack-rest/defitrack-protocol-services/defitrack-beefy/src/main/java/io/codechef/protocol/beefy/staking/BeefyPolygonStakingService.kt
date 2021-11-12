package io.codechef.protocol.beefy.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.codechef.common.network.Network
import io.codechef.defitrack.abi.ABIResource
import io.codechef.defitrack.staking.UserStakingService
import io.codechef.defitrack.staking.domain.StakingElement
import io.codechef.defitrack.staking.domain.StakingMarketElement
import io.codechef.defitrack.staking.domain.VaultRewardToken
import io.codechef.defitrack.token.TokenService
import io.codechef.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.codechef.ethereumbased.contract.multicall.MultiCallElement
import io.codechef.matic.config.PolygonContractAccessor
import io.codechef.protocol.Protocol
import io.codechef.protocol.beefy.apy.BeefyAPYService
import io.codechef.protocol.beefy.contract.BeefyVaultContract
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class BeefyPolygonStakingService(
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val polygonStakingMarketService: BeefyPolygonStakingMarketService,
    objectMapper: ObjectMapper,
    tokenService: TokenService
) : UserStakingService(tokenService, objectMapper) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    override fun getStaking(address: String, vaultId: String): StakingElement? {
       return polygonStakingMarketService.marketBuffer.firstOrNull {
           it.id == vaultId
       }?.let {

            val contract = BeefyVaultContract(
                polygonContractAccessor,
                vaultV6ABI,
                it.contractAddress,
                it.id
            )

            vaultToStakingElement(address, contract.balanceOf(address)).invoke(it)
        }
    }

    override fun getStakings(address: String): List<StakingElement> {
        val markets = polygonStakingMarketService.marketBuffer

        return polygonContractAccessor.readMultiCall(
            markets.map {
                val contract = BeefyVaultContract(
                    polygonContractAccessor,
                    vaultV6ABI,
                    it.contractAddress,
                    it.id
                )
                MultiCallElement(
                    contract.createFunction(
                        "balanceOf",
                        inputs = listOf(address.toAddress()),
                        outputs = listOf(
                            TypeReference.create(Uint256::class.java)
                        )
                    ),
                    contract.address
                )
            }).mapIndexed { index, balance ->
            vaultToStakingElement(address, balance[0].value as BigInteger)(markets[index])
        }.filterNotNull()
    }

    private fun vaultToStakingElement(address: String, balance: BigInteger) = { market: StakingMarketElement ->
        try {
            if (balance > BigInteger.ZERO) {
                val contract = BeefyVaultContract(
                    polygonContractAccessor,
                    vaultV6ABI,
                    market.contractAddress,
                    market.id
                )

                val want = tokenService.getTokenInformation(market.token.address, getNetwork())
                val underlyingBalance = if (balance > BigInteger.ZERO) {
                    balance.toBigDecimal().times(contract.getPricePerFullShare.toBigDecimal())
                        .divide(BigDecimal.TEN.pow(18))
                } else {
                    BigDecimal.ZERO
                }

                StakingElement(
                    id = market.id,
                    network = getNetwork(),
                    protocol = getProtocol(),
                    user = address,
                    name = market.name,
                    rate = getAPY(market.id),
                    url = "https://polygon.beefy.finance/",
                    stakedToken =
                    vaultStakedToken(
                        want.address,
                        underlyingBalance.toBigInteger()
                    ),
                    rewardTokens = listOf(
                        VaultRewardToken(
                            name = want.name,
                            symbol = want.symbol,
                            decimals = want.decimals
                        )
                    ),
                    vaultType = "beefyVaultV6",
                    contractAddress = market.contractAddress
                )
            } else {
                null
            }
        } catch (ex: Exception) {
            logger.error("Problem with vault was: {}", market.contractAddress, ex)
            null
        }
    }

    private fun getAPY(vaultId: String): Double {
        return try {
            (beefyAPYService.getAPYS().getOrDefault(vaultId, null)?.times(BigDecimal(10000))
                ?.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)?.toDouble()) ?: 0.0
        } catch (ex: Exception) {
            ex.printStackTrace()
            0.0
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}