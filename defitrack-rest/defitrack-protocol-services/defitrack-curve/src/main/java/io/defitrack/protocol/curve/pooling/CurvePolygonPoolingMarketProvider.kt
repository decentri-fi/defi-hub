package io.defitrack.protocol.curve.pooling

import io.defitrack.protocol.crv.CurvePolygonPoolGraphProvider
import org.springframework.stereotype.Service

@Service
class CurvePolygonPoolingMarketProvider(
    curvePoolGraphProvider: CurvePolygonPoolGraphProvider,
) : CurvePoolingMarketProvider(curvePoolGraphProvider)