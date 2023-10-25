package io.defitrack.protocol.ovix

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.position.Position
import io.defitrack.market.position.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import io.defitrack.protocol.moonwell.MoonwellUnitRollerContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.OVIX)
class OvixZkEVMLendingMarketProvider : LendingMarketProvider() {

    val deferredComptroller = AsyncUtils.lazyAsync {
        getComptroller()
    }

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        deferredComptroller.await().getMarkets().map { market ->
            OvixUnitRollerContract(
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

    private suspend fun getTokenContracts(): List<CompoundTokenContract> {
        return getComptroller().getMarkets().map { market ->
            object : CompoundTokenContract(
                getBlockchainGateway(),
                market
            ) {
                override fun fallbackUnderlying(): String {
                    return "0x4F9A0e7FD2Bf6067db6994CF12E4495Df938E6e9"
                }
            }
        }
    }

    private suspend fun toLendingMarket(ctokenContract: CompoundTokenContract): LendingMarket? {
        return try {
            getToken(ctokenContract.getUnderlyingAddress()).let { underlyingToken ->
                val exchangeRate = ctokenContract.exchangeRate.await()
                create(
                    identifier = ctokenContract.address,
                    name = ctokenContract.readName(),
                    token = underlyingToken.toFungibleToken(),
                    marketSize = Refreshable.refreshable {
                        getPriceResource().calculatePrice(
                            PriceRequest(
                                underlyingToken.address,
                                getNetwork(),
                                ctokenContract.cash.await().add(ctokenContract.totalBorrows()).toBigDecimal()
                                    .asEth(underlyingToken.decimals),
                            )
                        ).toBigDecimal()
                    },
                    poolType = "ovix-lendingpool",
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
                        "oToken" to ctokenContract.address,
                    )
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private suspend fun getComptroller(): MoonwellUnitRollerContract {
        return MoonwellUnitRollerContract(
            getBlockchainGateway(),
            "0x6ea32f626e3a5c41547235ebbdf861526e11f482"
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.OVIX
    }

    override fun getNetwork(): Network {
        return Network.POLYGON_ZKEVM
    }
}