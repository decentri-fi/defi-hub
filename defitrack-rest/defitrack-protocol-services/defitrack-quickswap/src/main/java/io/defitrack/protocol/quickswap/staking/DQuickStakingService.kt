package io.defitrack.protocol.quickswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.quickswap.QuickswapService
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.RewardToken
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DQuickStakingService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource
) : UserStakingService(erC20Resource) {

    val dquickStakingABI by lazy {
        abiResource.getABI("quickswap/dquick.json")
    }

    val dquick = dquickContract()

    override fun getStakings(address: String): List<StakingElement> {
        val balance = erC20Resource.getBalance(getNetwork(), dquick.address, address)
        return if (balance > BigInteger.ZERO) {
            listOf(
                StakingElement(
                    user = address,
                    rewardTokens = listOf(
                        RewardToken(
                            name = "Quick",
                            symbol = "QUICK",
                            decimals = 18,
                        )
                    ),
                    stakedToken = stakedToken(
                        address = "0x831753dd7087cac61ab5644b308642cc1c33dc13",

                        type = TokenType.SINGLE
                    ),
                    vaultType = "quickswap-dquick",
                    id = "polygon-dquick-${dquick.address}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = "Dragon's Lair",
                    contractAddress = dquick.address,
                    amount = dquick.quickBalance(address)
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