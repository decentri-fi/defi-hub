package io.defitrack.protocol.compound.lending

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.lending.invest.CompoundLendingInvestmentPreparer
import io.defitrack.protocol.compound.v2.contract.CompoundComptrollerContract
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Component
class CompoundLendingMarketProvider(
) : LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        getTokenContracts().map {
            async {
                throttled {
                    toLendingMarket(it)
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.COMPOUND
    }

    private suspend fun toLendingMarket(ctokenContract: CompoundTokenContract): LendingMarket? {
        return try {
            getToken(ctokenContract.underlyingAddress()).let { underlyingToken ->
                val exchangeRate = ctokenContract.exchangeRate()
                create(
                    identifier = ctokenContract.address,
                    name = ctokenContract.name(),
                    rate = getSupplyRate(compoundTokenContract = ctokenContract),
                    token = underlyingToken.toFungibleToken(),
                    marketSize = refreshable {
                        getPriceResource().calculatePrice(
                            PriceRequest(
                                underlyingToken.address,
                                getNetwork(),
                                ctokenContract.cash().add(ctokenContract.totalBorrows()).toBigDecimal()
                                    .asEth(underlyingToken.decimals),
                                TokenType.SINGLE
                            )
                        ).toBigDecimal()
                    },
                    poolType = "compound-lendingpool",
                    positionFetcher = PositionFetcher(
                        ctokenContract.address,
                        { user -> balanceOfFunction(user) },
                        { retVal ->
                            val tokenBalance = retVal[0].value as BigInteger
                            Position(
                                tokenBalance.times(exchangeRate).asEth().toBigInteger(),
                                tokenBalance
                            )
                        }
                    ),
                    investmentPreparer = CompoundLendingInvestmentPreparer(
                        ctokenContract,
                        getERC20Resource()
                    ),
                    marketToken = getToken(ctokenContract.address).toFungibleToken(),
                    erc20Compatible = true,
                    totalSupply = refreshable {
                        with(getToken(ctokenContract.address)) {
                            totalSupply.asEth(decimals)
                        }
                    }
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    suspend fun getSupplyRate(compoundTokenContract: CompoundTokenContract): BigDecimal {
        val blocksPerDay = 6463
        val dailyRate =
            (compoundTokenContract.supplyRatePerBlock().toBigDecimal().divide(BigDecimal.TEN.pow(18)) * BigDecimal(
                blocksPerDay
            )) + BigDecimal.ONE

        return dailyRate.pow(365).minus(BigDecimal.ONE).times(BigDecimal.TEN.pow(4))
            .divide(BigDecimal.TEN.pow(4), 4, RoundingMode.HALF_UP)
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

    private suspend fun getTokenContracts(): List<CompoundTokenContract> {
        return getComptroller().getMarkets().map { market ->
            CompoundTokenContract(
                getBlockchainGateway(),
                market
            )
        }
    }

    private suspend fun getComptroller(): CompoundComptrollerContract {
        return CompoundComptrollerContract(
            getBlockchainGateway(),
            CompoundAddressesProvider.CONFIG[getNetwork()]!!.v2Controller!!
        )
    }
}