package io.defitrack.protocol.application.ironbank.lending

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ironbank.IronBankComptrollerContract
import io.defitrack.protocol.ironbank.IronBankService
import io.defitrack.protocol.ironbank.IronbankTokenContract
import io.defitrack.protocol.application.ironbank.lending.invest.CompoundLendingInvestmentPreparer
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
                        GetPriceCommand(
                            underlyingToken.address,
                            getNetwork(),
                            ctokenContract.cash()
                                .add(ctokenContract.totalBorrows())
                                .asEth(underlyingToken.decimals)
                        )
                    ).toBigDecimal()
                },
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
                    getERC20Resource(), balanceResource
                ),
                marketToken = ctoken,
                erc20Compatible = true,
                poolType = "iron-bank.lending",
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
            getIronBankTokenContract(market)
        }
    }

    private fun getIronBankTokenContract(market: String): IronbankTokenContract = with(getBlockchainGateway()) {
        return IronbankTokenContract(market)
    }

    private fun getComptroller(): IronBankComptrollerContract {
        return IronBankComptrollerContract(
            getBlockchainGateway(),
            ironBankService.getComptroller()
        )
    }
}