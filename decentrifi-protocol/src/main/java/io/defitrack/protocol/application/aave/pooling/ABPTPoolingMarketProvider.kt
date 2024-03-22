package io.defitrack.protocol.application.aave.pooling

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.domain.asShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerPoolTokenContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AAVE)
class ABPTPoolingMarketProvider : PoolingMarketProvider(
) {

    val abptAddress = "0x41a08648c3766f9f9d85598ff102a08f4ef84f84"
    val aaveAddress = "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"
    val wethAddress = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
    val bPool = "0xC697051d1C6296C24aE3bceF39acA743861D9A81"

    //TODO: test this
    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val aave = getToken(aaveAddress)
        val weth = getToken(wethAddress)

        val balancerPoolToken = createContract {
            BalancerPoolTokenContract(bPool)
        }

        return create(
            identifier = "abpt",
            address = abptAddress,
            name = "Aave Balance Pool Token",
            symbol = "ABPT",
            type = "abpt",
            breakdown = refreshable {
                listOf(
                    aave.asShare(balancerPoolToken.getBalance(aaveAddress)),
                    weth.asShare(balancerPoolToken.getBalance(wethAddress))
                )
            },
            totalSupply = refreshable {
                getToken(abptAddress).totalDecimalSupply()
            }
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.AAVE_V3
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

}