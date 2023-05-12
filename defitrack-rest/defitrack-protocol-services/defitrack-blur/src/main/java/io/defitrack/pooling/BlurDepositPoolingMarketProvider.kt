package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenType
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BlurDepositPoolingMarketProvider : PoolingMarketProvider() {

    val blurEthDeposit = "0x0000000000a39bb272e79075ade125fd351887ac"

    val blurEthDepositContract by lazy {
        ERC20Contract(
            getBlockchainGateway(),
            "", blurEthDeposit
        )
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        val ether = getToken("0x0")
        listOf(
            create(
                name = "BlurEth",
                identifier = blurEthDeposit,
                marketSize = calculateMarketSize(),
                address = blurEthDeposit,
                symbol = "blurEth",
                tokenType = TokenType.BLUR,
                tokens = listOf(ether.toFungibleToken()),
                totalSupply = blurEthDepositContract.totalSupply(),
                positionFetcher = defaultPositionFetcher(blurEthDeposit),
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.BLUR
    }

    private suspend fun calculateMarketSize(): BigDecimal {
        val balance = getBlockchainGateway().getNativeBalance(blurEthDeposit)
        return getPriceResource().calculatePrice(
            PriceRequest(
                "0x0",
                getNetwork(),
                balance
            )
        ).toBigDecimal()
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}