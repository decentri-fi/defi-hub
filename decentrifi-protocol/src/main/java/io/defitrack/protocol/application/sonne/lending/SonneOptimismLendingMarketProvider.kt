package io.defitrack.protocol.application.sonne.lending

import arrow.fx.coroutines.parMap
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.application.compound.lending.invest.CompoundLendingInvestmentPreparer
import io.defitrack.protocol.compound.v2.contract.CompoundComptrollerContract
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigInteger

@ConditionalOnCompany(Company.SONNE)
@Component
class SonneOptimismLendingMarketProvider : LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        getTokenContracts().parMap {
            toLendingMarket(it)
        }.filterNotNull()
    }

    private suspend fun toLendingMarket(ctokenContract: CompoundTokenContract): LendingMarket? {
        return try {
            getToken(ctokenContract.getUnderlyingAddress()).let { underlyingToken ->
                val exchangeRate = ctokenContract.exchangeRate.await()
                val cToken = getToken(ctokenContract.address)

                create(
                    identifier = ctokenContract.address,
                    name = cToken.name,
                    token = underlyingToken,
                    marketSize = refreshable {
                        getPriceResource().calculatePrice(
                            GetPriceCommand(
                                underlyingToken.address,
                                getNetwork(),
                                ctokenContract.cash.await().add(ctokenContract.totalBorrows.await())
                                    .asEth(underlyingToken.decimals),
                            )
                        ).toBigDecimal()
                    },
                    positionFetcher = PositionFetcher(ctokenContract::balanceOfFunction) { retVal ->
                        val tokenBalance = retVal[0].value as BigInteger
                        Position(
                            tokenBalance.times(exchangeRate).asEth().toBigInteger(),
                            tokenBalance
                        )
                    },
                    investmentPreparer = CompoundLendingInvestmentPreparer(
                        ctokenContract,
                        getERC20Resource(),
                        balanceResource
                    ),
                    marketToken = getToken(ctokenContract.address),
                    erc20Compatible = true,
                    poolType = "sonne.lending",
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

    private suspend fun getTokenContracts(): List<CompoundTokenContract> = with(getBlockchainGateway()) {
        return getComptroller().getMarkets().map { market ->
            CompoundTokenContract(market)
        }
    }

    private suspend fun getComptroller(): CompoundComptrollerContract = with(getBlockchainGateway()) {
        return CompoundComptrollerContract("0x60CF091cD3f50420d50fD7f707414d0DF4751C58")
    }

    override fun getProtocol(): Protocol {
        return Protocol.SONNE
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}