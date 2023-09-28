package io.defitrack.alienbase.farming

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardProvider
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.protocol.BasedDistributorV2Contract
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

@Component
class AlienbaseClaimableFarmingMarketProvider(
    private val farmingMarketProvider: AlienbaseFarmingMarketProvider
): ClaimableRewardProvider() {
    override suspend fun claimables(address: String): List<Claimable> {
        val markets = farmingMarketProvider.getMarkets()

       return  getBlockchainGateway().readMultiCall(markets.map {
            val contract = it.internalMetadata["contract"] as BasedDistributorV2Contract
            val poolId = it.internalMetadata["poolId"] as Int

            MultiCallElement(
                contract.pendingFunction(poolId, address),
                contract.address
            )
        }).mapIndexed { index, result ->
            if (result.success) {
                val amounts = (result.data[3].value as List<Uint256>).map { it.value as BigInteger }
                val addresses = (result.data[0].value as List<Address>).map { it.value as String }
                val market = markets[index]
                amounts.mapIndexedNotNull { amountIndex, amount ->
                    if (amount > BigInteger.ZERO) {
                        val token = erC20Resource.getTokenInformation(getNetwork(), addresses[amountIndex])
                        Claimable(
                            amount = amount,
                            network = getNetwork(),
                            id = "rwrd_${market.id}_${index}_${amountIndex}",
                            protocol = getProtocol(),
                            claimableToken = token.toFungibleToken(),
                            name = "${market.name} ${token.name} Reward"
                        )
                    } else {
                        null
                    }
                }
            } else {
                emptyList()
            }
        }.flatten()
    }

    override fun getProtocol(): Protocol {
        return Protocol.ALIENBASE
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}