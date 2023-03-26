package io.defitrack.protocol.curve.pooling

import io.defitrack.protocol.crv.CurveFantomGraphProvider
import org.springframework.stereotype.Component

@Component
class CurveFantomPoolingMarketProvider(
    curvePoolGraphProvider: CurveFantomGraphProvider,
) : CurvePoolingMarketProvider(curvePoolGraphProvider)