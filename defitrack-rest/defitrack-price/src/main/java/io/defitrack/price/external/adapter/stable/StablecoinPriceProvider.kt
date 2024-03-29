package io.defitrack.price.external.adapter.stable

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.port.output.ERC20Client
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class StablecoinPriceProvider(private val erC20ClientResource: ERC20Client) : ExternalPriceService {

    override fun importOrder(): Int {
        return 2
    }

    fun isStable(network: Network): suspend (String) -> Boolean {
        return {
            stableCoins.await().getOrDefault(network, emptyList()).map {
                it.address.lowercase()
            }.contains(it)
        }
    }

    val stableCoins = AsyncUtils.lazyAsync {
        mapOf(
            Network.OPTIMISM to listOf(
                erC20ClientResource.getTokenInformation(Network.OPTIMISM, "0x94b008aa00579c1307b0ef2c499ad98a8ce58e58"),
                erC20ClientResource.getTokenInformation(Network.OPTIMISM, "0x0b2c639c533813f4aa9d7837caf62653d097ff85"),
                erC20ClientResource.getTokenInformation(Network.OPTIMISM, "0x7f5c764cbc14f9669b88837ca1490cca17c31607"),
                erC20ClientResource.getTokenInformation(Network.OPTIMISM, "0xda10009cbd5d07dd0cecc66161fc93d7c9000da1"),
                erC20ClientResource.getTokenInformation(Network.OPTIMISM, "0x2e3d870790dc77a83dd1d18184acc7439a53f475"),
            ),
            Network.ARBITRUM to listOf(
                erC20ClientResource.getTokenInformation(Network.ARBITRUM, "0xaf88d065e77c8cc2239327c5edb3a432268e5831"),
                erC20ClientResource.getTokenInformation(Network.ARBITRUM, "0xfd086bc7cd5c481dcc9c85ebe478a1c0b69fcbb9"),
                erC20ClientResource.getTokenInformation(Network.ARBITRUM, "0xff970a61a04b1ca14834a43f5de4533ebddb5cc8"),
                erC20ClientResource.getTokenInformation(Network.ARBITRUM, "0xda10009cbd5d07dd0cecc66161fc93d7c9000da1"),
            ),
            Network.ETHEREUM to listOf(
                erC20ClientResource.getTokenInformation(Network.ETHEREUM, "0xdac17f958d2ee523a2206206994597c13d831ec7"),
                erC20ClientResource.getTokenInformation(Network.ETHEREUM, "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"),
                erC20ClientResource.getTokenInformation(Network.ETHEREUM, "0x6b175474e89094c44da98b954eedeac495271d0f"),
                erC20ClientResource.getTokenInformation(Network.ETHEREUM, "0x0000000000085d4780B73119b644AE5ecd22b376"),
                erC20ClientResource.getTokenInformation(Network.ETHEREUM, "0x4fabb145d64652a948d72533023f6e7a623c7c53"),
            ),
            Network.BASE to listOf(
                erC20ClientResource.getTokenInformation(Network.BASE, "0x833589fcd6edb6e08f4c7c32d4f71b54bda02913"),
                erC20ClientResource.getTokenInformation(Network.BASE, "0x50c5725949a6f0c72e6c4a641f24049a917db0cb"),
                erC20ClientResource.getTokenInformation(Network.BASE, "0xda3de145054ed30ee937865d31b500505c4bdfe7"),
                erC20ClientResource.getTokenInformation(Network.BASE, "0xd9aaec86b65d86f6a7b5b1b0c42ffa531710b6ca"),
            ),
            Network.POLYGON_ZKEVM to listOf(
                erC20ClientResource.getTokenInformation(Network.POLYGON_ZKEVM, "0x1e4a5963abfd975d8c9021ce480b42188849d41d"),
                erC20ClientResource.getTokenInformation(Network.POLYGON_ZKEVM, "0xa8ce8aee21bc2a48a5ef670afcc9274c7bbbc035"),
                erC20ClientResource.getTokenInformation(Network.POLYGON_ZKEVM, "0xc5015b9d9161dca7e18e32f6f25c4ad850731fd4"),
                erC20ClientResource.getTokenInformation(Network.POLYGON_ZKEVM, "0xFf8544feD5379D9ffa8D47a74cE6b91e632AC44D"),
            ),
            Network.POLYGON to listOf(
                erC20ClientResource.getTokenInformation(Network.POLYGON, "0xc2132d05d31c914a87c6611c10748aeb04b58e8f"),
                erC20ClientResource.getTokenInformation(Network.POLYGON, "0x3c499c542cEF5E3811e1192ce70d8cC03d5c3359"),
                erC20ClientResource.getTokenInformation(Network.POLYGON, "0x2791bca1f2de4661ed88a30c99a7a9449aa84174"),
                erC20ClientResource.getTokenInformation(Network.POLYGON, "0xdab529f40e671a1d4bf91361c21bf9f0c9712ab7"),
                erC20ClientResource.getTokenInformation(Network.POLYGON, "0x8f3cf7ad23cd3cadbd9735aff958023239c6a063"),
                erC20ClientResource.getTokenInformation(Network.POLYGON, "0x2e1ad108ff1d8c782fcbbb89aad783ac49586756"),
            )
        )
    }

    override suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return stableCoins.await()[token.network.toNetwork()]?.any {
            it.address.lowercase() == token.address.lowercase()
        } ?: false
    }

    override suspend fun getAllPrices(): Flow<ExternalPrice> {
        return stableCoins.await().flatMap {
            it.value
        }.map {
            ExternalPrice(
                it.address.lowercase(),
                it.network.toNetwork(),
                BigDecimal.ONE,
                "hardcoded",
                "hardcoded",
                importOrder()
            )
        }.asFlow()
    }
}