package io.defitrack.protocol.curve.pooling

import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurveArbitrumGraphProvider
import org.springframework.stereotype.Component

@Component
class CurveArbitrumPoolingMarketProvider(
    curvePoolGraphProvider: CurveArbitrumGraphProvider,
) : CurvePoolingMarketProvider(curvePoolGraphProvider) {

}