package io.defitrack.protocol.blur.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.map
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.BLUR)
class BlurDepositPoolingMarketProvider : PoolingMarketProvider() {

    val blurEthDeposit = "0x0000000000a39bb272e79075ade125fd351887ac"

    val blurEthDepositContract = lazyAsync {
        ERC20Contract(
            getBlockchainGateway(),
            blurEthDeposit
        )
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val contract = blurEthDepositContract.await()

        return coroutineScope {
            val ether = getToken("0x0")
            listOf(
                create(
                    name = "BlurEth",
                    identifier = blurEthDeposit,
                    marketSize = refreshable {
                        calculateMarketSize()
                    },
                    address = blurEthDeposit,
                    symbol = "blurEth",
                    tokens = listOf(ether.toFungibleToken()),
                    totalSupply = contract.totalSupply().map {
                        it.asEth(contract.decimals())
                    },
                    positionFetcher = defaultPositionFetcher(blurEthDeposit),
                )
            )
        }
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