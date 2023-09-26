package io.defitrack.protocol.curve.pooling

import io.defitrack.protocol.crv.CurveOptimismGraphProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class CurveOptimismPoolingMarketProvider(
    curvePoolGraphProvider: CurveOptimismGraphProvider,
) : CurvePoolingMarketProvider(curvePoolGraphProvider)