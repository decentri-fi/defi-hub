package io.defitrack.protocol.blur.pooling

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigDecimal.TEN

@Component
@ConditionalOnCompany(Company.BLUR)
class BlurDepositPoolingMarketProvider : PoolingMarketProvider() {

    val blurEthDeposit = "0x0000000000a39bb272e79075ade125fd351887ac"

    val blurEthDepositContract = lazyAsync {

    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = with(getBlockchainGateway()) {
        val contract = ERC20Contract(
            blurEthDeposit
        )

        val ether = getToken("0x0")
        return create(
            breakdown = refreshable {
                PoolingMarketTokenShare(
                    ether,
                    getBlockchainGateway().getNativeBalance(blurEthDeposit).times(TEN.pow(18)).toBigInteger()
                ).nel()
            },
            name = "BlurEth",
            identifier = blurEthDeposit,
            address = blurEthDeposit,
            symbol = "blurEth",
            tokens = listOf(ether),
            totalSupply = contract.totalSupply().map {
                it.asEth(contract.readDecimals().toInt())
            },
            positionFetcher = defaultPositionFetcher(blurEthDeposit),
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.BLUR
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}