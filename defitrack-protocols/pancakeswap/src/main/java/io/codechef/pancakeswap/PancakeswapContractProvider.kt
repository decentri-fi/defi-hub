package io.defitrack.pancakeswap

import io.defitrack.bsc.BscContractAccessor
import io.defitrack.protocol.Chain
import io.defitrack.protocol.Exchange
import io.defitrack.protocol.Type
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStream

@Component
class PancakeswapContractProvider(
    private val resourceLoader: ResourceLoader,
    val routerAddress: String = "0x05fF2B0DB69458A0750badebc4f9e13aDd608C7F",
    val bscContractAccessor: BscContractAccessor
) {

    val routerABI = getABI("router.json")

    @Bean
    fun providePancakeSwap(): Exchange {
        return Exchange(
            PancakeSwapRouter(routerAddress, routerABI, bscContractAccessor),
            Chain.BSC,
            "pancakeswap",
            Type.AMM,
            3L
        )
    }

    private fun getABI(abiName: String) = resourceLoader.getResource(abiName).inputStream.getContents()

    fun InputStream.getContents(): String = this.bufferedReader().use(BufferedReader::readText)

}