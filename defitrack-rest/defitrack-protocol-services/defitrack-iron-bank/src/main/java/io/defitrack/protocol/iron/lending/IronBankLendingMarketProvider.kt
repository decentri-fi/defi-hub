package io.defitrack.protocol.iron.lending

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.common.utils.Refreshable.Companion.refreshable
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
                val ctoken = getToken(ctokenContract.address)
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
                                ctokenContract.cash()
                                    .add(ctokenContract.totalBorrows()).toBigDecimal()
                                    .asEth(underlyingToken.decimals)
                            )
                        ).toBigDecimal()
                    },
                    poolType = "iron-bank-lendingpool",
                    positionFetcher = PositionFetcher(
                        ctokenContract.address,
                        { user -> balanceOfFunction(user) },
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
                    ),
                    marketToken = ctoken.toFungibleToken(),
                    erc20Compatible = true,
                    totalSupply = refreshable(ctokenContract.totalSupply().asEth(ctoken.decimals)) {
                        val ctoken = getToken(ctokenContract.address)
                        ctokenContract.totalSupply().asEth(ctoken.decimals)
                    }
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