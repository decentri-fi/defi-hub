package io.defitrack.protocol.ovix

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import io.defitrack.protocol.moonwell.MoonwellUnitRollerContract
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.OVIX)
class OvixZkEVMLendingMarketProvider : LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> {
        getComptroller().getMarkets().map { market ->
            unitroller(market)
        }
        return getTokenContracts().parMapNotNull(concurrency = 8) {
            toLendingMarket(it)
        }
    }

    private fun unitroller(market: String) = with(getBlockchainGateway()) {
        OvixUnitRollerContract(market)
    }

    private suspend fun getTokenContracts(): List<CompoundTokenContract> = with(getBlockchainGateway()) {
        return getComptroller().getMarkets().map { market ->
            object : CompoundTokenContract(
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

            val underlying = getToken(ctokenContract.getUnderlyingAddress())
            val ctoken = getToken(ctokenContract.address)

            underlying.let { underlyingToken ->
                val exchangeRate = ctokenContract.exchangeRate.await()
                create(
                    identifier = ctokenContract.address,
                    name = ctoken.name,
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
                    poolType = "ovix-lendingpool",
                    positionFetcher = PositionFetcher(
                        ctokenContract::balanceOfFunction
                    ) { retVal ->
                        val tokenBalance = retVal[0].value as BigInteger
                        Position(
                            tokenBalance.times(exchangeRate).asEth().toBigInteger(),
                            tokenBalance
                        )
                    },
                    marketToken = getToken(ctokenContract.address),
                    erc20Compatible = true,
                    totalSupply = refreshable(ctoken.totalDecimalSupply()) {
                        with(getToken(ctokenContract.address), FungibleTokenInformation::totalDecimalSupply)
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

    private suspend fun getComptroller(): MoonwellUnitRollerContract = with(getBlockchainGateway()) {
        return MoonwellUnitRollerContract(
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