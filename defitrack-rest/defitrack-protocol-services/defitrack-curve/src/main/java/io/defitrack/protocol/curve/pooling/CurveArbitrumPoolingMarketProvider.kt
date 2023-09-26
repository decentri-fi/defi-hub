package io.defitrack.protocol.curve.pooling

import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurveArbitrumGraphProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["arbitrum.enabled"], havingValue = "true", matchIfMissing = true)
class CurveArbitrumPoolingMarketProvider(
    curvePoolGraphProvider: CurveArbitrumGraphProvider,
) : CurvePoolingMarketProvider(curvePoolGraphProvider) {

}