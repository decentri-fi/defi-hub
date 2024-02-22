package io.defitrack.price.external.adapter.pendle

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.port.PriceResource
import io.defitrack.price.port.`in`.PriceCalculator
import io.defitrack.protocol.pendle.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

//TODO: test and enable
@ConditionalOnProperty("oracles.pendle.enabled", havingValue = "true", matchIfMissing = true)
class PendleOptimismPriceService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    pendleAddressBook: PendleAddressBook,
    priceResource: PriceCalculator
) : PendlePriceService(
    blockchainGatewayProvider, pendleAddressBook, priceResource, "154873897"
) {

    override fun getNetwork() = Network.OPTIMISM
}