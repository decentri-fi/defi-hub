package io.defitrack.price.decentrifi

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.token.DecentrifiERC20Resource
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.Executors
import kotlin.time.measureTime

@Component
class DecentrifiUniswapV3UnderlyingPriceRepository(
    private val httpClient: HttpClient,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val erC20Resource: DecentrifiERC20Resource
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    val prices = Cache.Builder<String, BigDecimal>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 1) // every 24 hours
    fun populatePrices() {
        Executors.newSingleThreadExecutor().submit {
            runBlocking {
                val duration = measureTime {
                    val pools = getUniswapV3Pools()

                    try {
                        importUsdPairs(pools)
                        importEthPairs(pools)
                    } catch (e: Exception) {
                        logger.error("Unable to fetch pools for UNISWAP_V3, result was ${e.message}")
                    }
                }
                logger.info("[took ${duration.inWholeSeconds} seconds] Uniswap V3 Underlying Price Repository populated with ${prices.asMap().entries.size} prices")
            }
        }
    }

    private suspend fun importEthPairs(pools: List<PoolingMarketVO>) {
        val eths = eths.await()
        val stables = stableCoins.await()

        val containsEth: (PoolingMarketVO) -> Boolean = {
            it.breakdown?.any { share ->
                (eths.get(it.network.toNetwork())?.address?.lowercase() == share.token.address.lowercase())
            } ?: false
        }

        val containsNoStables: (PoolingMarketVO) -> Boolean = {
            it.breakdown?.none { share ->
                stables.getOrDefault(it.network.toNetwork(), emptyList()).map { it.address.lowercase() }
                    .contains(share.token.address.lowercase())
            } ?: false
        }


        val ethPairs = pools.filter(containsEth).filter(containsEth).filter(containsNoStables)

        ethPairs.parMap(concurrency = 12) { pool ->
            try {

                val ethForNetwork = eths[pool.network.toNetwork()]?.address?.lowercase()

                if (ethForNetwork == null) {
                    logger.error("no known eth address found for network ${pool.network.name}")
                } else {

                    val ethPriceForNetwork =
                        prices.get(toIndex(pool.network.toNetwork(), ethForNetwork)) ?: BigDecimal.ZERO

                    if (ethPriceForNetwork == BigDecimal.ZERO) {
                        logger.error("price for eth was not prepopulated for network ${pool.network}")
                    }

                    val contract = UniswapV3PoolContract(
                        blockchainGatewayProvider.getGateway(pool.network.toNetwork()),
                        pool.address
                    )

                    val sqrtPriceX96 = contract.slot0().sqrtPriceX96
                    val token0 = erC20Resource.getTokenInformation(pool.network.toNetwork(), contract.token0.await())
                    val token1 = erC20Resource.getTokenInformation(pool.network.toNetwork(), contract.token1.await())

                    val priceInOtherToken = (sqrtPriceX96.toBigDecimal().dividePrecisely(
                        BigInteger.TWO.pow(96).toBigDecimal()
                    )).pow(2)

                    if (ethForNetwork == token0.address.lowercase()) {
                        val normalized = getNormalizedPrice(token0, token1, priceInOtherToken)
                        val price = ethPriceForNetwork.dividePrecisely(normalized)


                        val index = toIndex(pool.network.toNetwork(), token1.address)
                        if (prices.get(index) == null) {
                            prices.put(index, price)
                        }
                    } else if (ethForNetwork == token1.address.lowercase()) {
                        val normalized = ethPriceForNetwork.times(getNormalizedPrice(token0, token1, priceInOtherToken))
                        val index = toIndex(pool.network.toNetwork(), token0.address)
                        if (prices.get(index) == null) {
                            prices.put(index, normalized)
                        }
                    }
                }
            } catch (ex: Exception) {
                logger.error("Unable to fetch price for pool ${pool.address} on network ${pool.network}, result was ${ex.message}")
            }
        }
    }


    private suspend fun importUsdPairs(pools: List<PoolingMarketVO>) = coroutineScope {
        val stables = stableCoins.await()

        val containsStableCoin: (PoolingMarketVO) -> Boolean = {
            it.breakdown?.any { share ->
                stables.getOrDefault(it.network.toNetwork(), emptyList()).map { it.address.lowercase() }
                    .contains(share.token.address.lowercase())
            } ?: false
        }

        val usdPairs = pools.filter(containsStableCoin)

        val semaphore = Semaphore(12)

        usdPairs.forEach { pool ->
            launch {
                semaphore.withPermit {
                    try {

                        val stablesForNetwork = stables.getOrDefault(pool.network.toNetwork(), emptyList()).map {
                            it.address.lowercase()
                        }

                        val contract = UniswapV3PoolContract(
                            blockchainGatewayProvider.getGateway(pool.network.toNetwork()),
                            pool.address
                        )

                        val sqrtPriceX96 = contract.slot0().sqrtPriceX96
                        val token0 =
                            erC20Resource.getTokenInformation(pool.network.toNetwork(), contract.token0.await())
                        val token1 =
                            erC20Resource.getTokenInformation(pool.network.toNetwork(), contract.token1.await())

                        val priceInOtherToken = (sqrtPriceX96.toBigDecimal().dividePrecisely(
                            BigInteger.TWO.pow(96).toBigDecimal()
                        )).pow(2)

                        if (stablesForNetwork.contains(token0.address.lowercase())) {
                            val normalized = getNormalizedPrice(token0, token1, priceInOtherToken)
                            val price = BigDecimal.ONE.dividePrecisely(normalized)
                            prices.put(toIndex(pool.network.toNetwork(), token1.address), price)
                        } else if (stablesForNetwork.contains(token1.address.lowercase())) {
                            val normalized = getNormalizedPrice(token0, token1, priceInOtherToken)

                            prices.put(toIndex(pool.network.toNetwork(), token0.address), normalized)
                        }
                    } catch (ex: Exception) {
                        logger.error("Unable to fetch price for pool ${pool.address} on network ${pool.network}, result was ${ex.message}")
                    }
                }
            }
        }
    }

    private fun getNormalizedPrice(
        token0: TokenInformationVO,
        token1: TokenInformationVO,
        priceInOtherToken: BigDecimal
    ) = if (token0.decimals >= token1.decimals) {
        priceInOtherToken.times(
            BigDecimal.TEN.pow(token0.decimals - token1.decimals)
        )
    } else {
        priceInOtherToken.dividePrecisely(
            BigDecimal.TEN.pow(token1.decimals - token0.decimals)
        )
    }

    fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal? {
        return prices.get(toIndex(tokenInformationVO.network.toNetwork(), tokenInformationVO.address))
    }

    fun contains(tokenInformationVO: TokenInformationVO): Boolean {
        return prices.asMap()
            .containsKey(toIndex(tokenInformationVO.network.toNetwork(), tokenInformationVO.address))
    }

    suspend fun getUniswapV3Pools(): List<PoolingMarketVO> = withContext(Dispatchers.IO) {
        val result = httpClient.get("https://api.decentri.fi/uniswap_v3/pooling/all-markets")
        if (result.status.isSuccess()) result.body()
        else {
            logger.error("Unable to fetch pools for UNISWAP_V3, result was ${result.body<String>()}")
            emptyList()
        }
    }

    fun toIndex(network: Network, address: String): String {
        return network.name + "-" + address.lowercase()
    }

    val eths = lazyAsync {
        mapOf(
            Network.OPTIMISM to erC20Resource.getTokenInformation(
                Network.OPTIMISM,
                "0x4200000000000000000000000000000000000006"
            ),
            Network.BASE to erC20Resource.getTokenInformation(
                Network.BASE,
                "0x4200000000000000000000000000000000000006"
            ),
            Network.ETHEREUM to erC20Resource.getTokenInformation(
                Network.ETHEREUM,
                "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
            ),
            Network.ARBITRUM to erC20Resource.getTokenInformation(
                Network.ARBITRUM,
                "0x82af49447d8a07e3bd95bd0d56f35241523fbab1"
            )
        )
    }

    val stableCoins = lazyAsync {
        mapOf(
            Network.OPTIMISM to listOf(
                erC20Resource.getTokenInformation(Network.OPTIMISM, "0x94b008aa00579c1307b0ef2c499ad98a8ce58e58"),
                erC20Resource.getTokenInformation(Network.OPTIMISM, "0x0b2c639c533813f4aa9d7837caf62653d097ff85"),
                erC20Resource.getTokenInformation(Network.OPTIMISM, "0x7f5c764cbc14f9669b88837ca1490cca17c31607"),
                erC20Resource.getTokenInformation(Network.OPTIMISM, "0xda10009cbd5d07dd0cecc66161fc93d7c9000da1"),
                erC20Resource.getTokenInformation(Network.OPTIMISM, "0x2e3d870790dc77a83dd1d18184acc7439a53f475"),
            ),
            Network.ARBITRUM to listOf(
                erC20Resource.getTokenInformation(Network.ARBITRUM, "0xaf88d065e77c8cc2239327c5edb3a432268e5831"),
                erC20Resource.getTokenInformation(Network.ARBITRUM, "0xfd086bc7cd5c481dcc9c85ebe478a1c0b69fcbb9"),
                erC20Resource.getTokenInformation(Network.ARBITRUM, "0xff970a61a04b1ca14834a43f5de4533ebddb5cc8"),
                erC20Resource.getTokenInformation(Network.ARBITRUM, "0xda10009cbd5d07dd0cecc66161fc93d7c9000da1"),
            ),
            Network.ETHEREUM to listOf(
                erC20Resource.getTokenInformation(Network.ETHEREUM, "0xdac17f958d2ee523a2206206994597c13d831ec7"),
                erC20Resource.getTokenInformation(Network.ETHEREUM, "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"),
                erC20Resource.getTokenInformation(Network.ETHEREUM, "0x6b175474e89094c44da98b954eedeac495271d0f"),
                erC20Resource.getTokenInformation(Network.ETHEREUM, "0x0000000000085d4780B73119b644AE5ecd22b376"),
                erC20Resource.getTokenInformation(Network.ETHEREUM, "0x4fabb145d64652a948d72533023f6e7a623c7c53"),
            ),
            Network.BASE to listOf(
                erC20Resource.getTokenInformation(Network.BASE, "0x833589fcd6edb6e08f4c7c32d4f71b54bda02913"),
                erC20Resource.getTokenInformation(Network.BASE, "0x50c5725949a6f0c72e6c4a641f24049a917db0cb"),
                erC20Resource.getTokenInformation(Network.BASE, "0xda3de145054ed30ee937865d31b500505c4bdfe7"),
            )
        )
    }
}