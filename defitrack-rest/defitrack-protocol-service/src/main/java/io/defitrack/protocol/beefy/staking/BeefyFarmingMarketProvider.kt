package io.defitrack.protocol.beefy.staking

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.BulkConstantResolver
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
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

abstract class BeefyFarmingMarketProvider(
    private val beefyAPYService: BeefyAPYService,
    private val vaults: List<BeefyVault>,
    private val constantResolver: BulkConstantResolver,
) : FarmingMarketProvider() {


    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contracts = vaults.map { beefyVault ->
            BeefyVaultContract(
                getBlockchainGateway(),
                beefyVault.earnContractAddress,
                beefyVault
            )
        }

        constantResolver.resolve(contracts) //sideEffect

        return contracts.parMapNotNull(concurrency = 12) {
            toStakingMarketElement(it)
        }
    }

    private suspend fun toStakingMarketElement(contract: BeefyVaultContract): FarmingMarket? {
        return try {
            val want = getToken(contract.want())
            val token = getToken(contract.address)
            create(
                identifier = contract.beefyVault.id,
                name = "${want.name} Beefy Vault",
                apr = getAPY(contract),
                stakedToken = want,
                rewardToken = want,
                token = token,
                rewardsFinished = contract.beefyVault.status == "eol",
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
                    contract.fullExitFunction()
                },
                metadata = mapOf(
                    "vaultAddress" to contract.address,
                )
            )
        } catch (ex: Exception) {
            logger.error("Unable to get beefy farm ${contract.beefyVault.id}: {}", ex.message)
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
                beefyVault.balance.await().toBigDecimal()
                    .divide(BigDecimal.TEN.pow(want.decimals), 18, RoundingMode.HALF_UP),
                want.type
            )
        )
    )

    private suspend fun getAPY(contract: BeefyVaultContract): BigDecimal {
        return try {
            (beefyAPYService.getAPYS().getOrDefault(contract.beefyVault.id, BigDecimal.ZERO))
        } catch (ex: Exception) {
            BigDecimal.ZERO
        }
    }
}