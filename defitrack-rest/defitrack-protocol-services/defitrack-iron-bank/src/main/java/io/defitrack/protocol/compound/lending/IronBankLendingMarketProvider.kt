package io.defitrack.protocol.compound.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.IronBankComptrollerContract
import io.defitrack.protocol.compound.IronBankService
import io.defitrack.protocol.compound.IronbankTokenContract
import io.defitrack.protocol.compound.lending.invest.CompoundLendingInvestmentPreparer
import io.defitrack.token.TokenType
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Component
abstract class IronBankLendingMarketProvider(
    private val abiResource: ABIResource,
    private val compoundEthereumService: IronBankService,
    private val priceResource: PriceResource
) : LendingMarketProvider() {

    val comptrollerABI by lazy {
        runBlocking {
            abiResource.getABI("compound/comptroller.json")
        }
    }

    val cTokenABI by lazy {
        runBlocking {
            abiResource.getABI("compound/ctoken.json")
        }
    }


    override suspend fun fetchMarkets(): List<LendingMarket> =
        withContext(Dispatchers.IO.limitedParallelism(5)) {
            getTokenContracts().map {
                async {
                    toLendingMarket(it)
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
                    marketSize = priceResource.calculatePrice(
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
                            tokenBalance.times(exchangeRate).asEth(18).toBigInteger()
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
            compoundEthereumService.getComptroller()
        )
    }
}