package io.defitrack.protocol.application.synthetix

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.synthetix.SynthetixContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SYNTHETIX)
class SNXLendingMarketProvider : LendingMarketProvider() {

    val erc20ProxyAddress = "0xC011a73ee8576Fb46F5E1c5751cA3B9Fe0af2a6F"
    val snxAddress = "0xd0dA9cBeA9C3852C5d63A95F9ABCC4f6eA0F9032"

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<LendingMarket> {

        val snx = getToken(snxAddress)
        val snxProxy = getToken(erc20ProxyAddress)
        val snxContract = SynthetixContract(erc20ProxyAddress)

        return create(
            identifier = erc20ProxyAddress,
            name = "SNX",
            token = snx,
            marketToken = snxProxy,
            totalSupply = refreshable {
                getToken(erc20ProxyAddress).totalDecimalSupply()
            },
            poolType = "synthetix",
            positionFetcher = PositionFetcher(
                snxContract::collateralFn
            )
        ).nel()
    }

    override fun getProtocol(): Protocol = Protocol.SYNTHETIX

    override fun getNetwork(): Network = Network.ETHEREUM
}