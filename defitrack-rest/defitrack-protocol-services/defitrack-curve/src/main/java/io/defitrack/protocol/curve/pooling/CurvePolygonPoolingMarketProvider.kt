package io.defitrack.protocol.curve.pooling

import io.defitrack.protocol.crv.CurvePolygonPoolGraphProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = ["polygon.enabled"], havingValue = "true", matchIfMissing = true)
class CurvePolygonPoolingMarketProvider(
    curvePoolGraphProvider: CurvePolygonPoolGraphProvider,
) : CurvePoolingMarketProvider(curvePoolGraphProvider)