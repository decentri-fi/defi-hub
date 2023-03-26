package io.defitrack.protocol.iron.lending

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.IronBankComptrollerContract
import io.defitrack.protocol.compound.IronBankService
import io.defitrack.protocol.compound.IronbankTokenContract
import io.defitrack.protocol.iron.lending.invest.CompoundLendingInvestmentPreparer
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

abstract class IronBankLendingMarketProvider(
    private val ironBankService: IronBankService,
) : LendingMarketProvider() {

    val comptrollerABI by lazy {
        runBlocking {
            getAbi("compound/comptroller.json")
        }
    }

    val cTokenABI by lazy {
        runBlocking {
            getAbi("compound/ctoken.json")
        }
    }


    override suspend fun fetchMarkets(): List<LendingMarket> =
        coroutineScope {
            getTokenContracts().map {
                async {
                    throttled {
                        toLendingMarket(it)
                    }
                }
            }.awaitAll().filterNotNull()
        }

    private suspend fun toLendingMarket(ctokenContract: IronbankTokenContract): LendingMarket? {
        return try {
            val exchangeRate = ctokenContract.exchangeRate()
            getToken(ctokenContract.underlyingAddress()).let { underlyingToken ->
                create(
                    identifier = ctokenContract.address,
                    name = ctokenContract.name(),
                    rate = getSupplyRate(compoundTokenContract = ctokenContract),
                    token = underlyingToken.toFungibleToken(),
                    marketSize = getPriceResource().calculatePrice(
                        PriceRequest(
                            underlyingToken.address,
                            getNetwork(),
                            ctokenContract.cash().add(ctokenContract.totalBorrows()).toBigDecimal().dividePrecisely(
                                BigDecimal.TEN.pow(underlyingToken.decimals),
                            ),
                            TokenType.SINGLE
                        )
                    ).toBigDecimal(),
                    poolType = "iron-bank-lendingpool",
                    positionFetcher = PositionFetcher(
                        ctokenContract.address,
                        { user -> ctokenContract.balanceOfMethod(user) },
                        { retVal ->
                            val tokenBalance = retVal[0].value as BigInteger
                            Position(
                                tokenBalance.times(exchangeRate).asEth(18).toBigInteger(),
                                tokenBalance
                            )
                        }
                    ),
                    investmentPreparer = CompoundLendingInvestmentPreparer(
                        ctokenContract,
                        getERC20Resource()
                    )
                )
            }
        } catch (ex: Exception) {
            logger.error("Unable to get info for iron bank contract ${ctokenContract.address}")
            null
        }
    }

    suspend fun getSupplyRate(compoundTokenContract: IronbankTokenContract): BigDecimal {
        val blocksPerDay = 6463
        val dailyRate =
            (compoundTokenContract.supplyRatePerBlock().toBigDecimal().divide(BigDecimal.TEN.pow(18)) * BigDecimal(
                blocksPerDay
            )) + BigDecimal.ONE

        return dailyRate.pow(365).minus(BigDecimal.ONE).times(BigDecimal.TEN.pow(4))
            .divide(BigDecimal.TEN.pow(4), 4, RoundingMode.HALF_UP)
    }

    override fun getProtocol(): Protocol {
        return Protocol.IRON_BANK
    }

    private suspend fun getTokenContracts(): List<IronbankTokenContract> {
        return getComptroller().getMarkets().map { market ->
            IronbankTokenContract(
                getBlockchainGateway(),
                cTokenABI,
                market
            )
        }
    }

    private fun getComptroller(): IronBankComptrollerContract {
        return IronBankComptrollerContract(
            getBlockchainGateway(),
            comptrollerABI,
            ironBankService.getComptroller()
        )
    }
}