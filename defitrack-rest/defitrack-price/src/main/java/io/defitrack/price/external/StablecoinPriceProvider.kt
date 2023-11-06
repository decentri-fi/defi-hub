package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class StablecoinPriceProvider(private val erC20Resource: ERC20Resource) {

    val stableCoins = AsyncUtils.lazyAsync {
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