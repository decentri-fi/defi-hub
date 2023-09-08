package io.defitrack.protocol.compound.rewards

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardProvider
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.toMultiCall
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundEthereumService
import io.defitrack.protocol.compound.v3.contract.CompoundRewardContract
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class CompoundRewardProvider(
    private val compoundEthereumService: CompoundEthereumService
) : ClaimableRewardProvider() {

    val contract by lazy {
        CompoundRewardContract(getBlockchainGateway(), "0x1B0e765F6224C21223AeA2af16c1C46E38885a40")
    }

    override suspend fun claimables(address: String): List<Claimable> {
        val markets = compoundEthereumService.getV3Tokens()

        val results = getBlockchainGateway().readMultiCall(
            markets.map { cTokenAddress ->
                contract.getRewardOwedFn(cTokenAddress, address).toMultiCall(contract.address)
            }
        )

        return results.mapIndexed { index, result ->
            val positionSize = result[1].value as BigInteger
            if (positionSize > BigInteger.ZERO) {

                val rewardToken = result[0].value as String

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