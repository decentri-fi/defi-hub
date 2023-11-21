package io.defitrack.protocol.synthetix

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import javax.swing.text.Position

@Component
@ConditionalOnCompany(Company.SYNTHETIX)
class SNXLendingMarketProvider : LendingMarketProvider() {

    val erc20ProxyAddress = "0xC011a73ee8576Fb46F5E1c5751cA3B9Fe0af2a6F"
    val snxAddress = "0xd0dA9cBeA9C3852C5d63A95F9ABCC4f6eA0F9032"

    override suspend fun fetchMarkets(): List<LendingMarket> {

        val snx = getToken(snxAddress)
        val snxProxy = getToken(erc20ProxyAddress)
        val snxContract = SynthetixContract(getBlockchainGateway(), erc20ProxyAddress)

        return create(
            identifier = erc20ProxyAddress,
            name = "SNX",
            token = snx,
            marketToken = snxProxy,
            totalSupply = refreshable(snxProxy.totalDecimalSupply()) {
                getToken(erc20ProxyAddress).totalDecimalSupply()
            },
            poolType = "synthetix",
            positionFetcher = PositionFetcher(
                snxContract.address,
                snxContract::collateralFn
            )
        ).nel()
    }

    override fun getProtocol(): Protocol = Protocol.SYNTHETIX

    override fun getNetwork(): Network = Network.ETHEREUM
}