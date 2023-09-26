package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.CurveFactoryContract
import io.defitrack.protocol.crv.contract.CurvePoolContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnProperty(value = ["base.enabled"], havingValue = "true", matchIfMissing = true)
class CurveBasePoolingMarketProvider : CurvePoolingMarketProvider(
    "0x3093f9b57a428f3eb6285a589cb35bea6e78c336"
) {
    override fun getNetwork(): Network {
        return Network.BASE
    }
}