package io.defitrack.protocol.blur.pooling

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.map
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.BLUR)
class BlurStakingMarketMarketProvider : FarmingMarketProvider() {

    val blurAddress = "0x5283d291dbcf85356a21ba090e6db59121208b44"
    val blurStakingAddress = "0xec2432a227440139ddf1044c3fea7ae03203933e"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val blur = getToken(blurAddress)
        return create(
            name = "BlurEth",
            identifier = blurStakingAddress,
            positionFetcher = defaultPositionFetcher(blurStakingAddress),
            stakedToken = blur
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.BLUR
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}