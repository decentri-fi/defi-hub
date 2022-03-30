package io.defitrack.protocol.yearn

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessorConfig
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.protocol.Protocol
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class YearnUserStakingService(
    private val yearnService: YearnService,
    private val contractAccessorGateway: ContractAccessorGateway,
    abiResource: ABIResource,
    erC20Resource: ERC20Resource,
) : UserStakingService(
    erC20Resource
) {

    val erc20ABI = abiResource.getABI("general/ERC20.json")



    override fun getStakings(address: String): List<StakingElement> {
        val ethereumContractAccessor = contractAccessorGateway.getGateway(getNetwork())

        return runBlocking {
            val vaults = yearnService.provideYearnV2Vaults().filter {
                it.token.symbol.isNotBlank()
            }

            ethereumContractAccessor.readMultiCall(
                vaults.map { vault ->
                    MultiCallElement(
                        ERC20Contract(
                            ethereumContractAccessor,
                            erc20ABI,
                            vault.shareToken.id
                        ).balanceOfMethod(address),
                        vault.shareToken.id
                    )
                },
            ).mapIndexed { index, retVal ->
                val balance = retVal[0].value as BigInteger
                if (balance > BigInteger.ZERO) {

                    val stakedtoken =
                        erC20Resource.getTokenInformation(getNetwork(), vaults[index].token.id).toFungibleToken()

                    stakingElement(
                        vaultName = "Yearn $index Vault",
                        rewardTokens = listOf(stakedtoken),
                        stakedToken = stakedtoken,
                        vaultType = "yearn-v2",
                        vaultAddress = vaults[index].shareToken.id,
                        rate = 0.0,
                        id = "yearn-ethereum-v2-$index",
                        amount = balance
                    )
                } else {
                    null
                }
            }.filterNotNull()
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}