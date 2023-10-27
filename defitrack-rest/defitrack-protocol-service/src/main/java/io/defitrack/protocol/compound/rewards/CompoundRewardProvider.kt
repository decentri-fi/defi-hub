package io.defitrack.protocol.compound.rewards

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.claimable.ClaimableMarketProvider
import io.defitrack.claimable.domain.ClaimableMarket
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v3.contract.CompoundRewardContract
import io.defitrack.protocol.compound.v3.contract.CompoundV3AssetContract
import io.defitrack.transaction.PreparedTransaction

abstract class CompoundRewardProvider(
    val network: Network
) : ClaimableMarketProvider() {

    private val deferredContract = lazyAsync {
        getContract()
    }

    open fun getContract(): CompoundRewardContract {
        return object :
            CompoundRewardContract(blockchainGatewayProvider.getGateway(network), CompoundAddressesProvider.CONFIG[network]!!.rewards) {
            override suspend fun getRewardConfig(comet: String): RewardConfig {
                return (read(
                    "rewardConfig",
                    inputs = listOf(comet.toAddress()),
                    outputs = listOf(
                        TypeUtils.address(),
                        TypeUtils.uint64(),
                        TypeUtils.bool(),
                    )
                )[0].value as String).let {
                    RewardConfig(it)
                }
            }
        }
    }

    override suspend fun fetchClaimables(): List<ClaimableMarket> {
        val markets = CompoundAddressesProvider.CONFIG[network]!!.v3Tokens
        return markets.map { comet ->
            val asset = CompoundV3AssetContract(getBlockchainGateway(), comet)
            val basetoken = erC20Resource.getTokenInformation(network, asset.baseToken())
            val rewardToken = erC20Resource.getTokenInformation(
                network,
                deferredContract.await().getRewardConfig(asset.address).token
            )
            ClaimableMarket(
                id = "rwrd_${comet}",
                name = "compound ${basetoken.name} rewards",
                network = network,
                protocol = Protocol.COMPOUND,
                claimableRewardFetchers = listOf(ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken.toFungibleToken(),
                        contractAddress = deferredContract.await().address,
                        getRewardFunction = { user ->
                            deferredContract.await().getRewardOwedFn(comet, user)
                        }
                    ),
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            network.toVO(),
                            deferredContract.await().getRewardOwedFn(comet, user),
                            deferredContract.await().address,
                            user
                        )
                    }
                ))
            )
        }
    }

    protected fun getBlockchainGateway(): BlockchainGateway {
        return blockchainGatewayProvider.getGateway(network)
    }
}