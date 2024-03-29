package io.defitrack.price.external.adapter.pendle

import arrow.core.Either.Companion.catch
import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.`in`.PriceCalculator
import io.defitrack.price.port.out.ExternalPriceService
import io.defitrack.protocol.pendle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.slf4j.LoggerFactory
import java.math.BigDecimal

abstract class PendlePriceService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val pendleAddressBook: PendleAddressBook,
    private val priceCalculator: PriceCalculator,
    private val startBlock: String
) : ExternalPriceService {

    override fun importOrder(): Int {
        return 40
    }

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getPrices(): MutableList<ExternalPrice> {
        val prices = mutableListOf<ExternalPrice>()

        val gateway = blockchainGatewayProvider.getGateway(getNetwork())

        val pendlePtOracleContract = PendleOracleContract(
            gateway, getAddress().ptOracleContract
        )

        val factory = with(gateway) {
            PendleMarketFactoryContract(
                getAddress().marketFactoryV3
            )
        }

        factory.getMarkets(startBlock).forEach {
            catch {
                val market = with(gateway) {
                    PendleMarketContract(
                        address = it.market
                    )
                }

                val syContract = with(gateway) { PendleSyContract(market.readTokens().sy) }

                val ratio = pendlePtOracleContract.getPtToAssetRate(it.market)
                val asset = syContract.asset()

                prices.add(
                    ExternalPrice(
                        it.pt,
                        getNetwork(),
                        priceCalculator.calculatePrice(
                            GetPriceCommand(
                                asset,
                                getNetworkForToken(asset),
                                ratio
                            )
                        ).toBigDecimal(),
                        "pendle-${getNetwork().slug}",
                        "oracle",
                        importOrder()
                    )
                )

                prices.add(
                    ExternalPrice(
                        syContract.address.lowercase(),
                        getNetwork(),
                        priceCalculator.calculatePrice(
                            GetPriceCommand(
                                asset,
                                getNetworkForToken(asset),
                                BigDecimal.ONE
                            )
                        ).toBigDecimal(),
                        "pendle-${getNetwork().slug}",
                        "oracle",
                        importOrder()
                    )
                )
            }.mapLeft {
                logger.error("Error while fetching price for ${it.message}")
            }
        }

        logger.info("Pendle prices loaded with ${prices.size} entries")
        return prices
    }

    fun getNetworkForToken(address: String): Network {
        //weird things at pendle, jesus
        return if (address == "0xae7ab96520de3a18e5e111b5eaab095312d7fe84" || address == "0x35fa164735182de50811e8e2e824cfb9b6118ac2") {
            Network.ETHEREUM
        } else {
            getNetwork()
        }
    }


    override suspend fun getAllPrices(): Flow<ExternalPrice> = channelFlow {
        getPrices().forEach {
            send(it)
        }
    }

    private fun getAddress(): PendleAddressBook.PendleAddresses {
        return pendleAddressBook.addresses[getNetwork()] ?: throw RuntimeException("No addresses found for pendle")
    }

    abstract fun getNetwork(): Network
}