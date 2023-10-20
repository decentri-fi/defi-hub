package io.defitrack.protocol.maker.yield

import arrow.core.nonEmptyListOf
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.Position
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.makerdao.SDAiContract
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.MAKERDAO)
class SavingsDaiMarketProvider : FarmingMarketProvider() {

    val sdaiContractAddress = "0x83f20f44975d03b1b09e64809b757c47f942beea"
    val daiAddress = "0x6b175474e89094c44da98b954eedeac495271d0f"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val dai = getToken(daiAddress)
        val contract = SDAiContract(getBlockchainGateway(), sdaiContractAddress)
        return nonEmptyListOf(
            create(
                name = "sDAI",
                identifier = sdaiContractAddress,
                stakedToken = dai,
                rewardTokens = nonEmptyListOf(dai),
                positionFetcher = PositionFetcher(sdaiContractAddress, ERC20Contract::balanceOfFunction) {
                    val shares = it[0].value as BigInteger
                    Position(
                        contract.convertToAssets(shares),
                        shares
                    )
                }
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.MAKERDAO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}