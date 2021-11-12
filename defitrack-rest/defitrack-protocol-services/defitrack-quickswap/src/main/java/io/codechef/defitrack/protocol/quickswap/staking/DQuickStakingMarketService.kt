package io.codechef.defitrack.protocol.quickswap.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.codechef.common.network.Network
import io.codechef.defitrack.abi.ABIResource
import io.codechef.defitrack.staking.StakingMarketService
import io.codechef.defitrack.staking.domain.RewardToken
import io.codechef.defitrack.staking.domain.StakedToken
import io.codechef.defitrack.staking.domain.StakingMarketElement
import io.codechef.defitrack.token.TokenService
import io.codechef.matic.config.PolygonContractAccessor
import io.codechef.protocol.Protocol
import io.codechef.protocol.staking.TokenType
import io.codechef.quickswap.QuickswapService
import io.codechef.quickswap.contract.DQuickContract
import org.springframework.stereotype.Service

@Service
class DQuickStakingMarketService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiResource: ABIResource,
) : StakingMarketService {

    val dquickStakingABI by lazy {
        abiResource.getABI("quickswap/dquick.json")
    }

    val dquick = dquickContract()

    override fun getStakingMarkets(): List<StakingMarketElement> {
        return listOf(
            StakingMarketElement(
                id = "polygon-dquick-${dquick.address.lowercase()}",
                network = getNetwork(),
                protocol = getProtocol(),
                name = "DQuick",
                token = StakedToken(
                    name = "Quick",
                    symbol = "QUICK",
                    address = "0x831753dd7087cac61ab5644b308642cc1c33dc13",
                    network = getNetwork(),
                    decimals = 18,
                    type = TokenType.SINGLE
                ),
                rewardToken = RewardToken(
                    name = "Quick",
                    symbol = "QUICK",
                    decimals = 18,
                ),
                contractAddress = dquick.address,
                vaultType = "quickswap-dquick",
                marketSize = 0.0,
                rate = 0.0
            )
        )
    }

    private fun dquickContract() = DQuickContract(
        polygonContractAccessor,
        dquickStakingABI,
        quickswapService.getDQuickContract(),
    )

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}