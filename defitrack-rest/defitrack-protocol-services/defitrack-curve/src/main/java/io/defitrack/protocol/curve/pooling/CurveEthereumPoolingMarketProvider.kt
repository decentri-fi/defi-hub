package io.defitrack.protocol.curve.pooling

import io.defitrack.protocol.crv.CurveEthereumGraphProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["ethereum.enabled"], havingValue = "true", matchIfMissing = true)
class CurveEthereumPoolingMarketProvider(
    curvePoolGraphProvider: CurveEthereumGraphProvider,
) : CurvePoolingMarketProvider(curvePoolGraphProvider)