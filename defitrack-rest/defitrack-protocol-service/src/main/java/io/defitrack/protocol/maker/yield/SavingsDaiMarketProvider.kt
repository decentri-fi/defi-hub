package io.defitrack.protocol.maker.yield

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
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

        return create(
            name = "sDAI",
            identifier = sdaiContractAddress,
            stakedToken = dai,
            rewardToken = dai,
            positionFetcher = PositionFetcher(contract::balanceOfFunction) {
                val shares = it[0].value as BigInteger
                Position(
                    contract.convertToAssets(shares),
                    shares
                )
            }
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.MAKERDAO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}