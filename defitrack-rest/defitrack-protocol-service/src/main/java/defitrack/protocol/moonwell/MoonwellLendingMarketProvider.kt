package io.defitrack.protocol.moonwell

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.RewardDistributorContract
import io.defitrack.protocol.UnitRollerContract
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.MOONWELL)
class MoonwellLendingMarketProvider : LendingMarketProvider() {

    val deferredComptroller = lazyAsync {
        getComptroller()
    }

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        deferredComptroller.await().getMarkets().map { market ->
            CompoundTokenContract(
                getBlockchainGateway(),
                market
            )
        }
        getTokenContracts().map {
            async {
                throttled {
                    toLendingMarket(it)
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun toLendingMarket(ctokenContract: CompoundTokenContract): LendingMarket? {
        return try {
            getToken(ctokenContract.getUnderlyingAddress()).let { underlyingToken ->
                val exchangeRate = ctokenContract.exchangeRate.await()
                create(
                    identifier = ctokenContract.address,
                    name = ctokenContract.name(),
                    token = underlyingToken.toFungibleToken(),
                    marketSize = Refreshable.refreshable {
                        getPriceResource().calculatePrice(
                            PriceRequest(
                                underlyingToken.address,
                                getNetwork(),
                                ctokenContract.cash.await().add(ctokenContract.totalBorrows()).toBigDecimal()
                                    .asEth(underlyingToken.decimals),
                                TokenType.SINGLE
                            )
                        ).toBigDecimal()
                    },
                    poolType = "moonwell-lendingpool",
                    positionFetcher = PositionFetcher(
                        ctokenContract.address,
                        { user -> ERC20Contract.balanceOfFunction(user) },
                        { retVal ->
                            val tokenBalance = retVal[0].value as BigInteger
                            Position(
                                tokenBalance.times(exchangeRate).asEth().toBigInteger(),
                                tokenBalance
                            )
                        }
                    ),
                    marketToken = getToken(ctokenContract.address).toFungibleToken(),
                    erc20Compatible = true,
                    totalSupply = Refreshable.refreshable {
                        with(getToken(ctokenContract.address)) {
                            totalSupply.asEth(decimals)
                        }
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

    private suspend fun getComptroller(): UnitRollerContract {
        return UnitRollerContract(
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