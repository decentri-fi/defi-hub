package io.defitrack.protocol.moonwell

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.MOONWELL)
class MoonwellLendingMarketProvider : LendingMarketProvider() {


    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        getTokenContracts().parMap {
            toLendingMarket(it)
        }.filterNotNull()
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
                                    .asEth(underlyingToken.decimals),
                            )
                        ).toBigDecimal()
                    },
                    poolType = "moonwell-lendingpool",
                    positionFetcher = PositionFetcher(
                        ctokenContract::balanceOfFunction
                    ) { retVal ->
                        val tokenBalance = retVal[0].value as BigInteger
                        if (tokenBalance > BigInteger.ZERO) {
                            Position(
                                tokenBalance.times(exchangeRate.await()).asEth().toBigInteger(),
                                tokenBalance
                            )
                        } else Position.ZERO
                    },
                    marketToken = cToken,
                    erc20Compatible = true,
                    totalSupply = refreshable(cToken.totalDecimalSupply()) {
                        getToken(ctokenContract.address).totalDecimalSupply()
                    },
                    metadata = mapOf(
                        "mToken" to ctokenContract.address,
                    )
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private suspend fun getTokenContracts(): List<CompoundTokenContract> {
        return getComptroller().getMarkets().map { market ->
            CompoundTokenContract(
                getBlockchainGateway(),
                market
            )
        }
    }

    private suspend fun getComptroller(): MoonwellUnitRollerContract {
        return MoonwellUnitRollerContract(
            getBlockchainGateway(),
            "0xfbb21d0380bee3312b33c4353c8936a0f13ef26c"
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.MOONWELL
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}