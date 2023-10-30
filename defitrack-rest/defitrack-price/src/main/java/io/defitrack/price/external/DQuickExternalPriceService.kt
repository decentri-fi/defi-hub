package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.BeefyPricesService
import io.defitrack.price.PriceProvider
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.token.ERC20Resource
import io.github.reactivecircus.cache4k.Cache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

//@Component
class DQuickExternalPriceService(
    quickswapService: QuickswapService,
    private val erc20resource: ERC20Resource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : ExternalPriceService {

    @Autowired
    private lateinit var priceProvider: PriceProvider

    val dquickAddress = quickswapService.getOldDQuickContractAddress()

    val cache = Cache.Builder<String, BigDecimal>().expireAfterWrite(1.hours).build()

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

    override fun getAllPrices(): List<ExternalPrice> {
        return cache.get("dquick")?.let {
            ExternalPrice(
                dquickAddress,
                Network.POLYGON,
                it,
                "dquick"
            )
        }?.let {
            listOf(it)
        } ?: emptyList()
    }

    suspend fun getQuickPrice(): BigDecimal {
        return priceProvider.getPrice(
            erc20resource.getTokenInformation(Network.POLYGON, "0xB5C064F955D8e7F38fE0460C556a72987494eE17")
        )
    }
}