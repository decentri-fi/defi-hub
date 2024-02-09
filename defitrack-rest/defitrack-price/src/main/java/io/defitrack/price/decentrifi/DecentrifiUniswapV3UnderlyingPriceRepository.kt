package io.defitrack.price.decentrifi

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.market.domain.pooling.PoolingMarketInformation
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.BulkConstantResolver
import io.defitrack.marketinfo.port.out.Markets
import io.defitrack.price.external.ExternalPrice
import io.defitrack.price.external.StablecoinPriceProvider
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.Executors
import kotlin.time.measureTime

@Component
@ConditionalOnProperty("oracles.uniswap_v3.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiUniswapV3UnderlyingPriceRepository(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource,
    marketResource: Markets,
    stablecoinPriceProvider: StablecoinPriceProvider,
    bulkConstantResolver: BulkConstantResolver
) : DecentrifiUniswapV3BasedUnderlyingPriceRepository(
    blockchainGatewayProvider,
    erC20Resource,
    marketResource,
    stablecoinPriceProvider,
    bulkConstantResolver,
    Protocol.UNISWAP_V3
)