package io.defitrack.protocol.application.compound.rewards

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.claim.AbstractClaimableMarketProvider
import io.defitrack.claim.ClaimableMarket
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v3.contract.CompoundRewardContract
import io.defitrack.protocol.compound.v3.contract.CompoundV3AssetContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.beans.factory.annotation.Autowired

abstract class CompoundRewardProvider(
    val network: Network
) : AbstractClaimableMarketProvider() {

    @Autowired
    private lateinit var compoundAddressesProvider: CompoundAddressesProvider

    private val deferredContract = lazyAsync {
        getContract()
    }

    open fun getContract(): CompoundRewardContract {
        return object :
            CompoundRewardContract(
                blockchainGatewayProvider.getGateway(network),
                compoundAddressesProvider.CONFIG[network]!!.rewards
            ) {
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
        val markets = compoundAddressesProvider.CONFIG[network]!!.v3Tokens
        return markets.map { comet ->
            val asset = getAssetContract(comet)
            val basetoken = erC20Resource.getTokenInformation(network, asset.baseToken.await())
            val rewardToken = erC20Resource.getTokenInformation(
                network,
                deferredContract.await().getRewardConfig(asset.address).token
            )
            ClaimableMarket(
                id = "rwrd_${comet}",
                name = "compound ${basetoken.name} rewards",
                network = network,
                protocol = Protocol.COMPOUND,
                claimableRewardFetchers = listOf(
                    ClaimableRewardFetcher(
                        Reward(
                            token = rewardToken,
                            getRewardFunction = deferredContract.await().getRewardOwedFn(comet)
                        ),
                        preparedTransaction = selfExecutingTransaction(deferredContract.await().claimFn(comet))
                    )
                )
            )
        }
    }

    private fun getAssetContract(comet: String): CompoundV3AssetContract = with(getBlockchainGateway()) {
        return CompoundV3AssetContract(comet)
    }

    protected fun getBlockchainGateway(): BlockchainGateway {
        return blockchainGatewayProvider.getGateway(network)
    }
}