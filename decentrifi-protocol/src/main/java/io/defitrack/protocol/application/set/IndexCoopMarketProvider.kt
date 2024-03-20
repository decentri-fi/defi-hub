package io.defitrack.protocol.application.set

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.SetTokenContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SET)
class IndexCoopMarketProvider : PoolingMarketProvider() {

    val ethx2 = "0x65c4c0517025ec0843c9146af266a2c5a2d148a2"

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val tokenContract = with(getBlockchainGateway()) { SetTokenContract(ethx2) }
        val token = getToken(ethx2)

        val breakdown = refreshable {
            tokenContract.getPositions().map {
                val underlying = getToken(it.token)
                val reserve = it.amount.toBigDecimal()
                    .times(tokenContract.getPositionMultiplier().asEth())
                    .times(token.totalDecimalSupply()).toBigInteger()
                PoolingMarketTokenShare(
                    token = underlying,
                    reserve = reserve,
                )
            }
        }
        return create(
            identifier = ethx2,
            address = ethx2,
            name = token.name,
            symbol = token.symbol,
            tokens = breakdown.get().map { element -> element.token },
            breakdown = breakdown,
            apr = null,
            positionFetcher = defaultPositionFetcher(ethx2),
            investmentPreparer = null,
            totalSupply = refreshable(token.totalDecimalSupply()) {
                getToken(ethx2).totalDecimalSupply()
            }
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SET
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}