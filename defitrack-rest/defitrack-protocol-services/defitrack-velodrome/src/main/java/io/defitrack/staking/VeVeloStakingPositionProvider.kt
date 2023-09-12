package io.defitrack.staking

import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.market.farming.FarmingPositionProvider
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.protocol.contract.VeVeloContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class VeVeloStakingPositionProvider(
    private val veVeloStakingMarketProvider: VeVeloStakingMarketProvider
) : FarmingPositionProvider() {

    val deferredVeVeloContract = lazyAsync {
        VeVeloContract(
            veVeloStakingMarketProvider.getBlockchainGateway(),
            veVeloStakingMarketProvider.veVelo
        )
    }

    override suspend fun getStakings(protocol: String, address: String): List<FarmingPosition> {
        val contract = deferredVeVeloContract.await()
        val tokensIds = contract.getTokenIdsForOwner(address)
        val results = veVeloStakingMarketProvider.getBlockchainGateway().readMultiCall(
            tokensIds.map { contract.lockedFn(it) }.map {
                MultiCallElement(it, veVeloStakingMarketProvider.veVelo)
            }
        )

        return results.map {
            create(
                veVeloStakingMarketProvider.getMarkets().first(),
                it.data[0].value as BigInteger,
                it.data[0].value as BigInteger, //[1] is unlock itme
            )
        }
    }
}