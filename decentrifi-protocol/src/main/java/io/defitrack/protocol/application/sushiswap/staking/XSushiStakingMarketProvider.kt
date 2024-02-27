package io.defitrack.protocol.application.sushiswap.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.xsushi.XSushiContract
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.SUSHISWAP)
class XSushiStakingMarketProvider : FarmingMarketProvider() {

    private val xsushi = "0x8798249c2E607446EfB7Ad49eC89dD1865Ff4272"
    private val sushi = "0x6B3595068778DD592e39A122f4f5a5cF09C90fE2"

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val xSushiContract = with(getBlockchainGateway()) { XSushiContract(xsushi) }
        val sushiToken = getToken(sushi)
        val totalStakedSushi = getERC20Resource().getBalance(getNetwork(), sushi, xsushi)

        val ratio = totalStakedSushi.toBigDecimal().dividePrecisely(xSushiContract.totalSupply().get().toBigDecimal())


        return listOf(
            create(
                name = "xsushi",
                identifier = "xsushi",
                stakedToken = sushiToken,
                rewardToken = sushiToken,
                marketSize = refreshable {
                    getMarketSize(
                        sushiToken,
                        xsushi,
                    )
                },
                positionFetcher = PositionFetcher(
                    xSushiContract::balanceOfFunction
                ) { retVal ->
                    val userXSushi = (retVal[0].value as BigInteger)

                    if (userXSushi > BigInteger.ZERO) {
                        Position(
                            userXSushi.toBigDecimal().times(ratio).toBigInteger(),
                            userXSushi
                        )
                    } else Position.ZERO
                },
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}