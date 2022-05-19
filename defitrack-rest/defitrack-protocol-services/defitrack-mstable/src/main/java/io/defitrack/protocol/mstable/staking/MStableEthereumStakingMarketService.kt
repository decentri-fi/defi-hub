package io.defitrack.protocol.mstable.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mstable.MStableEthereumBoostedSavingsVaultContract
import io.defitrack.protocol.mstable.MStableEthereumService
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class MStableEthereumStakingMarketService(
    private val contractAccessorGateway: ContractAccessorGateway,
    private val mStableEthereumService: MStableEthereumService,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource
) : StakingMarketService() {

    val boostedSavingsVaultABI by lazy {
        abiResource.getABI("mStable/BoostedSavingsVault.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarket> = coroutineScope{
        val gateway = contractAccessorGateway.getGateway(getNetwork())

        mStableEthereumService.getBoostedSavingsVaults().map {
            MStableEthereumBoostedSavingsVaultContract(
                gateway,
                boostedSavingsVaultABI,
                it
            )
        }.map { contract ->
          async {
              try {
                  toStakingMarket(contract)
              } catch (ex: Exception) {
                  ex.printStackTrace()
                  null
              }
          }
        }.awaitAll().filterNotNull()
    }

    private fun toStakingMarket(contract: MStableEthereumBoostedSavingsVaultContract): StakingMarket {
        val stakingToken = erC20Resource.getTokenInformation(getNetwork(), contract.stakingToken)
        val rewardsToken = erC20Resource.getTokenInformation(getNetwork(), contract.rewardsToken)
        return StakingMarket(
            id = "mstable-ethereum-${contract.address}",
            name = contract.name,
            stakedToken = stakingToken.toFungibleToken(),
            rewardTokens = listOf(
                rewardsToken.toFungibleToken()
            ),
            contractAddress = contract.address,
            vaultType = "mstable-boosted-savings-vault",
            network = getNetwork(),
            protocol = getProtocol(),
            balanceFetcher = StakingMarketBalanceFetcher(
                address = contract.address,
                { user -> contract.rawBalanceOfFunction(user) }
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.MSTABLE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}