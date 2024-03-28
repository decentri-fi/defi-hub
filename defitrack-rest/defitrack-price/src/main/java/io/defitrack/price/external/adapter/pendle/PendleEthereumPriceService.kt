package io.defitrack.price.external.adapter.pendle

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.port.`in`.PriceCalculator
import io.defitrack.protocol.pendle.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("oracles.pendle.enabled", havingValue = "true", matchIfMissing = true)
class PendleEthereumPriceService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    pendleAddressBook: PendleAddressBook,
    priceResource: PriceCalculator
) : PendlePriceService(
    blockchainGatewayProvider, pendleAddressBook, priceResource, "18669498"
) {

    override fun getNetwork() = Network.ETHEREUM
}