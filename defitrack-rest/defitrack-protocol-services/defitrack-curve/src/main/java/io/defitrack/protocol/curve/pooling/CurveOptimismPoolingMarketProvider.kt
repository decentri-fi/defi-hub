package io.defitrack.protocol.curve.pooling

import io.defitrack.protocol.crv.CurveOptimismGraphProvider
import org.springframework.stereotype.Service

@Service
class CurveOptimismPoolingMarketProvider(
    curvePoolGraphProvider: CurveOptimismGraphProvider,
) : CurvePoolingMarketProvider(curvePoolGraphProvider)