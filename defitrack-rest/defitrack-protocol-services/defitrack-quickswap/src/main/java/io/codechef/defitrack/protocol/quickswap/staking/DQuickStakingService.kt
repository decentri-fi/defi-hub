package io.codechef.defitrack.protocol.quickswap.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.codechef.defitrack.staking.UserStakingService
import io.codechef.defitrack.staking.domain.StakingElement
import io.codechef.defitrack.staking.domain.VaultRewardToken
import io.codechef.defitrack.token.TokenService
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.staking.TokenType
import io.defitrack.quickswap.QuickswapService
import io.defitrack.quickswap.contract.DQuickContract
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DQuickStakingService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiResource: ABIResource,
    objectMapper: ObjectMapper,
    tokenService: TokenService
) : UserStakingService(tokenService, objectMapper) {

    val dquickStakingABI by lazy {
        abiResource.getABI("quickswap/dquick.json")
    }

    val dquick = dquickContract()

    override fun getStakings(address: String): List<StakingElement> {

        val balance = dquick.balanceOf(address)
        return if (balance > BigInteger.ZERO) {
            listOf(
                StakingElement(
                    user = address,
                    rewardTokens = listOf(
                        VaultRewardToken(
                            name = "Quick",
                            symbol = "QUICK",
                            daily = "",
                            url = "https://polygonscan.io/address/0x831753dd7087cac61ab5644b308642cc1c33dc13",
                        )
                    ),
                    stakedToken = vaultStakedToken(
                        "0x831753dd7087cac61ab5644b308642cc1c33dc13",
                        dquick.quickBalance(address),
                        type = TokenType.SINGLE
                    ),
                    vaultType = "quickswap-dquick",
                    id = "polygon-dquick-${dquick.address}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = "Dragon's Lair",
                    contractAddress = dquick.address
                )
            )
        } else {
            emptyList()
        }
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