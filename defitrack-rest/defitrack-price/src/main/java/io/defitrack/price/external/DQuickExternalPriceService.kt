package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.domain.FungibleToken
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.port.input.ERC20Resource
import io.defitrack.price.PriceProvider
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.github.reactivecircus.cache4k.Cache
import org.springframework.beans.factory.annotation.Autowired
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

    override suspend fun getPrice(fungibleToken: FungibleToken): BigDecimal {
        return cache.get("dquick") {
            val quickAmount = DQuickContract(
                blockchainGatewayProvider.getGateway(Network.POLYGON),
                dquickAddress
            ).dquickForQuick(BigInteger.ONE.times(BigInteger.TEN.pow(18)))

            quickAmount.toBigDecimal().times(getQuickPrice()).dividePrecisely(BigDecimal.TEN.pow(18))
        }
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
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