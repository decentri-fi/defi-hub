package io.defitrack.protocol.polycat.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.polycat.PolycatMasterChefContract
import io.defitrack.protocol.polycat.PolycatService
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class PolycatStakingService(
    private val polycatService: PolycatService,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
    private val contractAccessorGateway: ContractAccessorGateway
) : UserStakingService(erC20Resource) {

    val masterChefABI by lazy {
        abiResource.getABI("polycat/MasterChef.json")
    }

    override fun getStakings(address: String): List<StakingElement> {

        val polycatMasterChefContracts = polycatService.getPolycatFarms().map {
            PolycatMasterChefContract(
                contractAccessorGateway.getGateway(getNetwork()),
                masterChefABI,
                it
            )
        }

        return polycatMasterChefContracts.flatMap { masterChef ->
            (0 until masterChef.poolLength).mapNotNull { poolIndex ->
                val balance = masterChef.userInfo(address, poolIndex).amount

                if (balance > BigInteger.ZERO) {
                    val stakedtoken =
                        erC20Resource.getTokenInformation(getNetwork(), masterChef.poolInfo(poolIndex).lpToken)
                    val rewardToken = erC20Resource.getTokenInformation(getNetwork(), masterChef.rewardToken)

                    stakingElement(
                        id = "polycat-${masterChef.address}-${poolIndex}",
                        vaultName = stakedtoken.name + " Vault",
                        stakedToken = stakedtoken.toFungibleToken(),
                        rewardTokens = listOf(
                            rewardToken.toFungibleToken()
                        ),
                        vaultAddress = masterChef.address,
                        vaultType = "polycat-masterchef",
                        amount = balance
                    )
                } else {
                    null
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.POLYCAT
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}