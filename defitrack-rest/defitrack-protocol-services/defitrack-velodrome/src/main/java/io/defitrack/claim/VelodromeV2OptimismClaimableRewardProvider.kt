package io.defitrack.claim

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardProvider
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.VelodromeV2GaugeContract
import io.defitrack.staking.VelodromeV2GaugeMarketProvider
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.util.*

@Component
class VelodromeV2OptimismClaimableRewardProvider(
    private val velodromeV2GaugeMarketProvider: VelodromeV2GaugeMarketProvider,
) : ClaimableRewardProvider() {

    val velodrome by lazy {
        runBlocking {
            erC20Resource.getTokenInformation(
                velodromeV2GaugeMarketProvider.getNetwork(),
                "0x9560e827af36c94d2ac33a39bce1fe78631088db"
            )
        }
    }

    override suspend fun claimables(address: String): List<Claimable> {
        val markets = velodromeV2GaugeMarketProvider.getMarkets()

        return velodromeV2GaugeMarketProvider.getBlockchainGateway().readMultiCall(
            markets.map {
                val gauge = it.metadata["address"].toString()
                val contract = it.metadata["contract"] as VelodromeV2GaugeContract

                MultiCallElement(
                    contract.earnedFn(address),
                    gauge
                )
            }
        ).mapIndexed { index, result ->
            val result = result[0].value as BigInteger
            if (result > BigInteger.ZERO) {
                val market = markets[index]
                val contract = market.metadata["contract"] as VelodromeV2GaugeContract
                Claimable(
                    id = UUID.randomUUID().toString(),
                    name = market.name + " reward",
                    type = "velodrome-reward",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    claimableTokens = listOf(velodrome.toFungibleToken()),
                    amount = result,
                    claimTransaction = PreparedTransaction(
                        velodromeV2GaugeMarketProvider.getNetwork().toVO(),
                        contract.getRewardFn(address),
                        contract.address
                    )
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return velodromeV2GaugeMarketProvider.getProtocol()
    }

    override fun getNetwork(): Network {
        return velodromeV2GaugeMarketProvider.getNetwork()
    }
}