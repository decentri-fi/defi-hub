package io.defitrack.price

import io.defitrack.abi.ABIResource
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.quickswap.QuickswapService
import io.defitrack.quickswap.contract.DQuickContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

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

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(1)
    ).build<String, BigDecimal>()

    override fun getOracleName(): String {
        return "dquick";
    }

    override fun getPrice(): BigDecimal {
        return runBlocking {
            cache.get("dquick") {
                val quickAmount = DQuickContract(
                    polygonContractAccessor,
                    dquickStakingABI,
                    dquickAddress
                ).dquickForQuick(BigInteger.ONE.times(BigInteger.TEN.pow(18)))

                val tokenPrice = getQuickPrice()
                quickAmount.toBigDecimal().times(tokenPrice).divide(
                    BigDecimal.TEN.pow(18), 6, RoundingMode.HALF_UP
                )
            }
        }
    }

    fun getQuickPrice(): BigDecimal {
        return beefyPricesService.getPrices()
            .getOrDefault("QUICK", BigDecimal.ZERO)
    }
}