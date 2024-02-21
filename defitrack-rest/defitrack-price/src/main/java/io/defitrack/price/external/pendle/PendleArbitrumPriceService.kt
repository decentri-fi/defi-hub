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

@Component
class PendleArbitrumPriceService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    pendleAddressBook: PendleAddressBook,
    priceResource: PriceResource
) : PendlePriceService(
    blockchainGatewayProvider, pendleAddressBook, priceResource, "154873897"
) {

    override fun getNetwork() = Network.ARBITRUM
}