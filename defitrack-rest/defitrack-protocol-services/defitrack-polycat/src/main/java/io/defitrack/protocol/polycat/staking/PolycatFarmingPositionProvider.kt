package io.defitrack.protocol.polycat.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.polycat.contract.PolycatMasterChefContract
import io.defitrack.protocol.polycat.PolycatService
import io.defitrack.market.farming.FarmingPositionProvider
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class PolycatFarmingPositionProvider(
    private val polycatService: PolycatService,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : FarmingPositionProvider(erC20Resource) {

    val masterChefABI by lazy {
        abiResource.getABI("polycat/MasterChef.json")
    }

    override suspend fun getStakings(address: String): List<FarmingPosition> {

        val polycatMasterChefContracts = polycatService.getPolycatFarms().map {
            PolycatMasterChefContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
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