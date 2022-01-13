package io.defitrack.protocol.beefy.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.staking.domain.VaultRewardToken
import io.defitrack.token.ERC20Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class BeefyPolygonUserStakingService(
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val polygonStakingMarketService: BeefyPolygonStakingMarketService,
    erC20Resource: ERC20Resource,
    objectMapper: ObjectMapper,
) : UserStakingService(erC20Resource, objectMapper) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    override fun getStaking(address: String, vaultId: String): StakingElement? {
        return polygonStakingMarketService.getStakingMarkets().firstOrNull {
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
        val markets = polygonStakingMarketService.getStakingMarkets()

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

                val want = erC20Resource.getTokenInformation(getNetwork(), market.token.address)
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