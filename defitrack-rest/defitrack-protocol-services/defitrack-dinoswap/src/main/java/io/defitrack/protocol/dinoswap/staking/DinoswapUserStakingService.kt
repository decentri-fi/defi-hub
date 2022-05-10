package io.defitrack.protocol.dinoswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dinoswap.DinoswapFossilFarmsContract
import io.defitrack.protocol.dinoswap.DinoswapService
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DinoswapUserStakingService(
    private val dinoService: DinoswapService,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
    private val contractAccessorGateway: ContractAccessorGateway
) : UserStakingService(erC20Resource) {

    val fossilFarms by lazy {
        abiResource.getABI("polycat/FossilFarms.json")
    }

    override fun getStakings(address: String): List<StakingElement> {

        val polycatMasterChefContracts = dinoService.getDinoFossilFarms().map {
            DinoswapFossilFarmsContract(
                contractAccessorGateway.getGateway(getNetwork()),
                fossilFarms,
                it
            )
        }

        return polycatMasterChefContracts.flatMap { masterChef ->
            (0 until masterChef.poolLength).mapNotNull { poolIndex ->
                val balance = masterChef.userInfo(address, poolIndex).amount

                if (balance > BigInteger.ZERO) {
                    val stakedtoken =
                        erC20Resource.getTokenInformation(getNetwork(), masterChef.getLpTokenForPoolId(poolIndex))
                    val rewardToken = erC20Resource.getTokenInformation(getNetwork(), masterChef.rewardToken)

                    stakingElement(
                        id = "dinoswap-${masterChef.address}-${poolIndex}",
                        vaultName = stakedtoken.name + " Vault",
                        stakedToken = stakedtoken.toFungibleToken(),
                        rewardTokens = listOf(
                            rewardToken.toFungibleToken()
                        ),
                        vaultAddress = masterChef.address,
                        vaultType = "dinoswap-fossilfarm",
                        amount = balance
                    )
                } else {
                    null
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.DINOSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}