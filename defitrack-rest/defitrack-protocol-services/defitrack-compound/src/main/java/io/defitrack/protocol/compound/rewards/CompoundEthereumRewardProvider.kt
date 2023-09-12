package io.defitrack.protocol.compound.rewards

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardProvider
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v3.contract.CompoundRewardContract
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class CompoundEthereumRewardProvider(
) : ClaimableRewardProvider() {

    val deferredContract = lazyAsync {
        CompoundRewardContract(getBlockchainGateway(), CompoundAddressesProvider.CONFIG[getNetwork()]!!.rewards)
    }

    override suspend fun claimables(address: String): List<Claimable> {
        val contract = deferredContract.await()

        val markets = CompoundAddressesProvider.CONFIG[getNetwork()]!!.v3Tokens

        val results = contract.readMultiCall(
            markets.map { cTokenAddress ->
                contract.getRewardOwedFn(cTokenAddress, address)
            }
        )

        return results.mapIndexed { index, result ->
            val positionSize = result.data[1].value as BigInteger
            if (positionSize > BigInteger.ZERO) {

                val rewardToken = result.data[0].value as String

                val market = markets[index]

                Claimable(
                    "",
                    "",
                    type = "compoundv3",
                    getProtocol(),
                    getNetwork(),
                    listOf(
                        erC20Resource.getTokenInformation(getNetwork(), rewardToken).toFungibleToken()
                    ),
                    positionSize,
                    PreparedTransaction(
                        getNetwork().toVO(),
                        contract.getRewardOwedFn(market, address),
                        contract.address,
                        address
                    )
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.COMPOUND
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}