package io.defitrack.protocol.beefy.staking

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.Position
import io.defitrack.market.position.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.protocol.beefy.staking.invest.BeefyStakingInvestmentPreparer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

abstract class BeefyFarmingMarketProvider(
    private val beefyAPYService: BeefyAPYService,
    private val vaults: List<BeefyVault>,
) : FarmingMarketProvider() {


    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val semaphore = Semaphore(10)
        vaults.map {
            async {
                semaphore.withPermit {
                    toStakingMarketElement(it)
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun toStakingMarketElement(beefyVault: BeefyVault): FarmingMarket? {
        return try {
            val contract = BeefyVaultContract(
                getBlockchainGateway(),
                beefyVault.earnContractAddress,
                beefyVault.id
            )
            val want = getToken(contract.want())
            create(
                identifier = contract.vaultId,
                name = "${want.name} Beefy Vault",
                apr = getAPY(contract),
                stakedToken = want,
                rewardToken = want,
                marketSize = refreshable {
                    getMarketSize(want, contract)
                },
                positionFetcher = PositionFetcher(
                    contract.address,
                    ::balanceOfFunction,
                    extractBalance = { result ->
                        val balance = result[0].value as BigInteger

                        if (balance > BigInteger.ZERO) {
                            Position(
                                (balance.times(contract.pricePerFullShare.await())).asEth().toBigInteger(),
                                balance,
                            )
                        } else Position.ZERO
                    }
                ),
                investmentPreparer = BeefyStakingInvestmentPreparer(contract, getERC20Resource()),
                exitPositionPreparer = prepareExit {
                    PreparedExit(
                        contract.fullExitFunction(),
                        contract.address,
                    )
                },
                metadata = mapOf(
                    "vaultAddress" to contract.address,
                )
            )
        } catch (ex: Exception) {
            logger.error("Unable to get beefy farm ${beefyVault.id}")
            null
        }
    }

    private suspend fun getMarketSize(
        want: TokenInformationVO,
        beefyVault: BeefyVaultContract
    ) = BigDecimal.valueOf(
        getPriceResource().calculatePrice(
            PriceRequest(
                want.address,
                getNetwork(),
                beefyVault.balance().toBigDecimal()
                    .divide(BigDecimal.TEN.pow(want.decimals), 18, RoundingMode.HALF_UP),
                want.type
            )
        )
    )

    private suspend fun getAPY(beefyVault: BeefyVaultContract): BigDecimal {
        return try {
            (beefyAPYService.getAPYS().getOrDefault(beefyVault.vaultId, BigDecimal.ZERO))
        } catch (ex: Exception) {
            BigDecimal.ZERO
        }
    }

}