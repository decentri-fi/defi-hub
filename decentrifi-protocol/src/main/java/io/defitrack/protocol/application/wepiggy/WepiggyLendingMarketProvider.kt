package io.defitrack.protocol.application.wepiggy

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.v2.contract.CompoundComptrollerContract
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import io.defitrack.protocol.wepiggy.WepiggyPolygonService
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.WEPIGGY)
class WepiggyLendingMarketProvider(
    private val wepiggyPolygonService: WepiggyPolygonService,
) : LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        getTokenContracts().parMapNotNull(concurrency = 8) {
            toLendingMarket(it)
        }
    }

    private suspend fun toLendingMarket(ctokenContract: CompoundTokenContract): LendingMarket? {
        return try {
            getToken(ctokenContract.getUnderlyingAddress()).let { underlyingToken ->
                val cToken = getToken(ctokenContract.address)
                val exchangeRate = ctokenContract.exchangeRate
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
                                    .asEth(
                                        underlyingToken.decimals
                                    ),
                            )
                        ).toBigDecimal()
                    },
                    poolType = "compound-lendingpool",
                    positionFetcher = PositionFetcher(
                        ctokenContract::balanceOfFunction
                    ) { retVal ->
                        val tokenBalance = retVal[0].value as BigInteger
                        if (tokenBalance > BigInteger.ZERO) {
                            Position(
                                tokenBalance.times(exchangeRate.await()).asEth().toBigInteger(),
                                tokenBalance
                            )
                        } else {
                            Position.ZERO
                        }
                    },
                    marketToken = cToken,
                    erc20Compatible = true,
                    totalSupply = ctokenContract.totalSupply().map {
                        it.asEth(cToken.decimals)
                    },
                    deprecated = true
                )
            }
        } catch (ex: Exception) {
            logger.error("unable to get wepiggy lending market for address ${ctokenContract.address}")
            null
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.WEPIGGY
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    private suspend fun getTokenContracts(): List<CompoundTokenContract> = with(getBlockchainGateway()) {
        getComptroller().getMarkets().map { market ->
            CompoundTokenContract(market)
        }
    }

    private fun getComptroller(): CompoundComptrollerContract = with(getBlockchainGateway()) {
        CompoundComptrollerContract(wepiggyPolygonService.getComptroller())
    }
}