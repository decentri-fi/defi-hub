package io.defitrack.price.external.pendle

import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.external.ExternalPrice
import io.defitrack.price.external.ExternalPriceService
import io.defitrack.price.port.PriceResource
import io.defitrack.protocol.pendle.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

abstract class PendlePriceService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val pendleAddressBook: PendleAddressBook,
    private val priceResource: PriceResource,
    private val startBlock: String
) : ExternalPriceService {

    val logger = LoggerFactory.getLogger(this::class.java)

    data class PendlePrice(
        val ptAddress: String,
        val underlying: String,
        val ratio: BigDecimal
    )

    val cache = mutableMapOf<String, PendlePrice>()

    @Scheduled(fixedDelay = 1000 * 60 * 60) // every hour
    fun importPrices() = runBlocking {
        val gateway = blockchainGatewayProvider.getGateway(getNetwork())

        val pendlePtOracleContract = PendleOracleContract(
            gateway,getAddress().ptOracleContract
        )

        val factory = PendleMarketFactoryContract(
            gateway,
            getAddress().marketFactoryV3
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
            priceResource.calculatePrice(
                GetPriceCommand(
                    it.underlying,
                    getNetwork(),
                    it.ratio
                )
            ).toBigDecimal()
        } ?: BigDecimal.ZERO
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return cache.values.map {
            val price = priceResource.calculatePrice(
                GetPriceCommand(
                    it.underlying,
                    getNetwork(),
                    it.ratio
                )
            ).toBigDecimal()
            ExternalPrice(
                it.ptAddress,
                getNetwork(),
                price,
                "pendle-arbitrum",
                "pendle-arbitrum"
            )
        }
    }

    fun getAddress(): PendleAddressBook.PendleAddresses {
        return pendleAddressBook.addresses[getNetwork()] ?: throw RuntimeException("No addresses found for pendle")
    }

    abstract fun getNetwork(): Network
}