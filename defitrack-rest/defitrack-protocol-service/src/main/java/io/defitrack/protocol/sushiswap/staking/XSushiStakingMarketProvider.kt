package io.defitrack.protocol.sushiswap.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.Position
import io.defitrack.market.position.PositionFetcher
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

        val xSushiContract = XSushiContract(getBlockchainGateway(), xsushi)
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
                    xsushi,
                    ::balanceOfFunction
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