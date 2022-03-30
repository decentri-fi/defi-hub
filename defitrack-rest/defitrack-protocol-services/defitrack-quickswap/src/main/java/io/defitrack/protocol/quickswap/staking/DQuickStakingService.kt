package io.defitrack.protocol.quickswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.polygon.config.PolygonContractAccessorConfig
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DQuickStakingService(
    private val quickswapService: QuickswapService,
    private val contractAccessorGateway: ContractAccessorGateway,
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
            val dquickToken = erC20Resource.getTokenInformation(getNetwork(), dquick.address).toFungibleToken()
            val quickToken = erC20Resource.getTokenInformation(getNetwork(), "0x831753dd7087cac61ab5644b308642cc1c33dc13").toFungibleToken()
            listOf(
                StakingElement(
                    rewardTokens = listOf(
                       dquickToken
                    ),
                    stakedToken = quickToken,
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
        contractAccessorGateway.getGateway(getNetwork()),
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