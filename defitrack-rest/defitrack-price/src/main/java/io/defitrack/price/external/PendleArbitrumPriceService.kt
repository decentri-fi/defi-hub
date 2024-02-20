package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceCalculator
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.port.PriceResource
import io.defitrack.protocol.pendle.PendleMarketContract
import io.defitrack.protocol.pendle.PendleMarketFactoryContract
import io.defitrack.protocol.pendle.PendleOracleContract
import io.defitrack.protocol.pendle.PendleSyContract
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PendleArbitrumPriceService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : ExternalPriceService {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var priceCalculator: PriceResource

    data class PendlePrice(
        val ptAddress: String,
        val underlying: String,
        val ratio: BigDecimal
    )

    val cache = mutableMapOf<String, PendlePrice>()

    @Scheduled(fixedDelay = 1000 * 60 * 60) // every hour
    fun getPts() = runBlocking {
        val gateway = blockchainGatewayProvider.getGateway(Network.ARBITRUM)

        val pendlePtOracleContract = PendleOracleContract(
            gateway, "0x7e16e4253CE4a1C96422a9567B23b4b5Ebc207F1"
        )

        val factory = PendleMarketFactoryContract(
            gateway,
            "0x2FCb47B58350cD377f94d3821e7373Df60bD9Ced"
        )

        factory.getMarkets("154873897").forEach {

            val market = PendleMarketContract(
                blockchainGateway = gateway,
                address = it.market
            )

            val syContract = PendleSyContract(
                gateway, market.readTokens().sy
            )

            val ratio = pendlePtOracleContract.getPtToAssetRate(it.market)
            val asset = syContract.yieldToken()

            val ptPrice = PendlePrice(it.pt, asset, ratio)
            val syPrice = PendlePrice(syContract.address, asset, BigDecimal.ONE)

            cache[it.pt.lowercase()] = ptPrice
            cache[syContract.address.lowercase()] = syPrice
        }

        logger.info("Pendle prices loaded with ${cache.entries.size} entries")
    }


    override suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return cache.containsKey(token.address.lowercase())
    }

    override suspend fun getPrice(fungibleToken: FungibleTokenInformation): BigDecimal {
        return cache[fungibleToken.address.lowercase()]?.let {
            priceCalculator.calculatePrice(
                GetPriceCommand(
                    it.underlying,
                    Network.ARBITRUM,
                    it.ratio
                )
            ).toBigDecimal()
        } ?: BigDecimal.ZERO
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return cache.values.map {
            val price = priceCalculator.calculatePrice(
                GetPriceCommand(
                    it.underlying,
                    Network.ARBITRUM,
                    it.ratio
                )
            ).toBigDecimal()
            ExternalPrice(
                it.ptAddress,
                Network.ARBITRUM,
                price,
                "pendle-arbitrum",
                "pendle-arbitrum"
            )
        }
    }
}