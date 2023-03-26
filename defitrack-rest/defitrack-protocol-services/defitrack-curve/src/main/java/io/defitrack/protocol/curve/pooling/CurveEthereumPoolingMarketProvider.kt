package io.defitrack.protocol.curve.pooling

import io.defitrack.protocol.crv.CurveEthereumGraphProvider
import org.springframework.stereotype.Component

@Component
class CurveEthereumPoolingMarketProvider(
    curvePoolGraphProvider: CurveEthereumGraphProvider,
) : CurvePoolingMarketProvider(curvePoolGraphProvider)