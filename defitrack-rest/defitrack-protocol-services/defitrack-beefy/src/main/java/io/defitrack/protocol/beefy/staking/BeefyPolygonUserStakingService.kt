package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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
) : UserStakingService(erC20Resource) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    override fun getStaking(address: String, vaultId: String): StakingElement? {
        return polygonStakingMarketService.getStakingMarkets().firstOrNull {
            it.id == vaultId
        }?.let {
            vaultToStakingElement(erC20Resource.getBalance(getNetwork(), it.contractAddress, address)).invoke(
                it
            )
        }
    }

    override fun getStakings(address: String): List<StakingElement> {
        val markets = polygonStakingMarketService.getStakingMarkets()

        return erC20Resource.getBalancesFor(address, markets.map { it.contractAddress }, polygonContractAccessor)
            .mapIndexed { index, balance ->
                vaultToStakingElement(balance)(markets[index])
            }.filterNotNull()
    }

    private fun vaultToStakingElement(balance: BigInteger) = { market: StakingMarketElement ->
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
                        .dividePrecisely(BigDecimal.TEN.pow(18))
                } else {
                    BigDecimal.ZERO
                }

                StakingElement(
                    id = market.id,
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = market.name,
                    rate = getAPY(market.id),
                    stakedToken = want.toFungibleToken(),
                    amount = underlyingBalance.toBigInteger(),
                    rewardTokens = listOf(
                        want.toFungibleToken()
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