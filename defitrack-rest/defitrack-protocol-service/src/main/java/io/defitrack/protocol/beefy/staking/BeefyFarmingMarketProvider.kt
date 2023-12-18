package io.defitrack.protocol.beefy.staking

import arrow.core.Either
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.erc20.FungibleToken
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.protocol.beefy.staking.invest.BeefyStakingInvestmentPreparer
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

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contracts = resolve(
            vaults.map { beefyVault ->
                BeefyVaultContract(
                    getBlockchainGateway(),
                    beefyVault.earnContractAddress,
                    beefyVault
                )
            }
        )

        return contracts.parMapNotNull(concurrency = 12) { beefy ->
            Either.catch {
                toStakingMarketElement(beefy)
            }.fold(
                ifLeft = {
                    logger.error("Failed to create market for ${beefy.beefyVault}", it)
                    null
                },
                ifRight = {
                    it
                }
            )
        }
    }

    private suspend fun toStakingMarketElement(contract: BeefyVaultContract): FarmingMarket {
        val want = getToken(contract.want())
        val token = getToken(contract.address)
        return create(
            identifier = contract.beefyVault.id,
            name = "${want.name} Beefy Vault",
            apr = getAPY(contract),
            stakedToken = want,
            rewardToken = want,
            token = token,
            deprecated = contract.beefyVault.status == "eol",
            marketSize = refreshable {
                getMarketSize(want, contract)
            },
            positionFetcher = PositionFetcher(
                contract::balanceOfFunction,
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
    }

    private suspend fun getMarketSize(
        want: FungibleToken,
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