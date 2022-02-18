package io.defitrack.price

import io.defitrack.abi.ABIResource
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.protocol.quickswap.QuickswapService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

@Component
class DQuickExternalPriceService(
    quickswapService: QuickswapService,
    private val beefyPricesService: BeefyPricesService,
    private val abiResource: ABIResource,
    private val polygonContractAccessor: PolygonContractAccessor,
) : ExternalPriceService {

    val dquickStakingABI by lazy {
        abiResource.getABI("quickswap/dquick.json")
    }

    val dquickAddress = quickswapService.getDQuickContract()

    val cache = Cache.Builder().expireAfterWrite(
        1.hours
    ).build<String, BigDecimal>()

    override fun getOracleName(): String {
        return "dquick"
    }

    override fun getPrice(): BigDecimal {
        return runBlocking {
            cache.get("dquick") {
                val quickAmount = DQuickContract(
                    polygonContractAccessor,
                    dquickStakingABI,
                    dquickAddress
                ).dquickForQuick(BigInteger.ONE.times(BigInteger.TEN.pow(18)))

                quickAmount.toBigDecimal().times(getQuickPrice()).dividePrecisely(BigDecimal.TEN.pow(18))
            }
        }
    }

    fun getQuickPrice(): BigDecimal {
        return beefyPricesService.getPrices()
            .getOrDefault("QUICK", BigDecimal.ZERO)
    }
}