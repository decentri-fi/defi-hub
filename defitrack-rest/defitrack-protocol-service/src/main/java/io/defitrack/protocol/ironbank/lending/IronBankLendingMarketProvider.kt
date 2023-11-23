package io.defitrack.protocol.ironbank.lending

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.map
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.position.Position
import io.defitrack.market.position.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ironbank.IronBankComptrollerContract
import io.defitrack.protocol.ironbank.IronBankService
import io.defitrack.protocol.ironbank.IronbankTokenContract
import io.defitrack.protocol.ironbank.lending.invest.CompoundLendingInvestmentPreparer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger

abstract class IronBankLendingMarketProvider(
    private val ironBankService: IronBankService,
) : LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> {
        return getTokenContracts().parMapNotNull(concurrency = 8) { contract ->
            catch {
                toLendingMarket(contract)
            }.mapLeft {
                logger.error("Unable to get lending market {}: {}", contract.address, it.message)
                null
            }.getOrNull()
        }
    }

    private suspend fun toLendingMarket(ctokenContract: IronbankTokenContract): LendingMarket {
        return getToken(ctokenContract.underlyingAddress()).let { underlyingToken ->
            val ctoken = getToken(ctokenContract.address)
            create(
                identifier = ctokenContract.address,
                name = ctoken.name,
                token = underlyingToken,
                marketSize = refreshable {
                    getPriceResource().calculatePrice(
                        PriceRequest(
                            underlyingToken.address,
                            getNetwork(),
                            ctokenContract.cash()
                                .add(ctokenContract.totalBorrows())
                                .asEth(underlyingToken.decimals)
                        )
                    ).toBigDecimal()
                },
                poolType = "iron-bank-lendingpool",
                positionFetcher = PositionFetcher(
                    ctokenContract::balanceOfFunction
                ) { retVal ->
                    val tokenBalance = retVal[0].value as BigInteger
                    if (tokenBalance > BigInteger.ZERO) {
                        Position(
                            tokenBalance.times(ctokenContract.exchangeRate.await()).asEth(ctoken.decimals)
                                .toBigInteger(),
                            tokenBalance
                        )
                    } else {
                        Position.ZERO
                    }
                },
                investmentPreparer = CompoundLendingInvestmentPreparer(
                    ctokenContract,
                    getERC20Resource()
                ),
                marketToken = ctoken,
                erc20Compatible = true,
                totalSupply = ctokenContract.totalSupply().map {
                    it.asEth(ctoken.decimals)
                }
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.IRON_BANK
    }

    private suspend fun getTokenContracts(): List<IronbankTokenContract> {
        return getComptroller().getMarkets().map { market ->
            IronbankTokenContract(
                getBlockchainGateway(),
                market
            )
        }
    }

    private fun getComptroller(): IronBankComptrollerContract {
        return IronBankComptrollerContract(
            getBlockchainGateway(),
            ironBankService.getComptroller()
        )
    }
}