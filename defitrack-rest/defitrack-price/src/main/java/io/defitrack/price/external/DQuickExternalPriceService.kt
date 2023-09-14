package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.BeefyPricesService
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

@Component
class DQuickExternalPriceService(
    quickswapService: QuickswapService,
    private val beefyPricesService: BeefyPricesService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : ExternalPriceService {

    val dquickAddress = quickswapService.getOldDQuickContractAddress()

    val cache = Cache.Builder<String, BigDecimal>().expireAfterWrite(
        1.hours
    ).build()

    override fun getOracleName(): String {
        return "dquick"
    }

    override suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal {
        return cache.get("dquick") {
            val quickAmount = DQuickContract(
                blockchainGatewayProvider.getGateway(Network.POLYGON),
                dquickAddress
            ).dquickForQuick(BigInteger.ONE.times(BigInteger.TEN.pow(18)))

            quickAmount.toBigDecimal().times(getQuickPrice()).dividePrecisely(BigDecimal.TEN.pow(18))
        }
    }

    suspend fun getQuickPrice(): BigDecimal {
        return beefyPricesService.getPrices()
            .getOrDefault("QUICK", BigDecimal.ZERO)
    }
}