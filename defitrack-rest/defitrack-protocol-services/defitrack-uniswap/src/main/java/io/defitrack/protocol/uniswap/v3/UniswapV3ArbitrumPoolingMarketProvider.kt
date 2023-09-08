package io.defitrack.protocol.uniswap.v3

import com.google.gson.JsonParser
import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

//@Component
class UniswapV3ArbitrumPoolingMarketProvider() : UniswapV3PoolingMarketProvider(
    listOf("165"),
    "0x1f98431c8ad98523631ae4a59f267346ea31f984"
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}