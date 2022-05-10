package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class BeefyFantomUserStakingService(
    contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val stakingMarketService: BeefyFantomStakingMarketService,
    erC20Resource: ERC20Resource
) : UserStakingService(erC20Resource) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    val gateway = contractAccessorGateway.getGateway(getNetwork())

    override fun getStakings(address: String): List<StakingElement> {
        val markets = stakingMarketService.getStakingMarkets()

        return erC20Resource.getBalancesFor(address, markets.map { it.contractAddress }, getNetwork())
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val market = markets[index]

                    try {
                        if (balance > BigInteger.ZERO) {
                            val contract = BeefyVaultContract(
                                gateway,
                                vaultV6ABI,
                                market.contractAddress,
                                market.id
                            )

                            val underlyingBalance = if (balance > BigInteger.ZERO) {
                                balance.toBigDecimal().times(contract.getPricePerFullShare.toBigDecimal())
                                    .divide(BigDecimal.TEN.pow(18))
                            } else {
                                BigDecimal.ZERO
                            }

                            StakingElement(
                                market = market,
                                amount = underlyingBalance.toBigInteger()
                            )
                        } else {
                            null
                        }
                    } catch (ex: Exception) {
                        logger.error("Problem with vault was: {}", market.contractAddress, ex)
                        null
                    }
                } else {
                    null
                }
            }.filterNotNull()
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
        return Network.FANTOM
    }
}