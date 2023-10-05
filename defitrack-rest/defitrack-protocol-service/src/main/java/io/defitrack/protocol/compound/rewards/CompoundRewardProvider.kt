package io.defitrack.protocol.compound.rewards

import io.defitrack.claimable.*
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import ClaimableMarketProvider
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v3.contract.CompoundRewardContract
import io.defitrack.protocol.compound.v3.contract.CompoundV3AssetContract
import io.defitrack.transaction.PreparedTransaction

abstract class CompoundRewardProvider(
) : UserClaimableProvider(), ClaimableMarketProvider {

    private val deferredContract = lazyAsync {
        getContract()
    }

    open fun getContract(): CompoundRewardContract {
        return object: CompoundRewardContract(getBlockchainGateway(), CompoundAddressesProvider.CONFIG[getNetwork()]!!.rewards) {
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

    override suspend fun getClaimables(): List<ClaimableMarket> {
        val markets = CompoundAddressesProvider.CONFIG[getNetwork()]!!.v3Tokens
        return markets.map { comet ->
            val asset = CompoundV3AssetContract(getBlockchainGateway(), comet)
            val basetoken = erC20Resource.getTokenInformation(getNetwork(), asset.baseToken())
            val rewardToken = erC20Resource.getTokenInformation(
                getNetwork(),
                deferredContract.await().getRewardConfig(asset.address).token
            )
            ClaimableMarket(
                id = "rwrd_${comet}",
                name = "compound ${basetoken.name} rewards",
                network = getNetwork(),
                protocol = getProtocol(),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken.toFungibleToken(),
                        contractAddress = deferredContract.await().address,
                        getRewardFunction = { user ->
                            deferredContract.await().getRewardOwedFn(comet, user)
                        }
                    ),
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            getNetwork().toVO(),
                            deferredContract.await().getRewardOwedFn(comet, user),
                            deferredContract.await().address,
                            user
                        )
                    }
                )
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.COMPOUND
    }

    override suspend fun claimables(address: String): List<UserClaimable> {
        return emptyList()
    }
}