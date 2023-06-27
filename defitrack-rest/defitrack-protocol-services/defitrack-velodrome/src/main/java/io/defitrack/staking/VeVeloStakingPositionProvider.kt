package io.defitrack.staking

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

    val veVeloContract by lazy {
        runBlocking {
            VeVeloContract(
                veVeloStakingMarketProvider.getBlockchainGateway(),
                veVeloStakingMarketProvider.veVelo
            )
        }
    }

    override suspend fun getStakings(protocol: String, address: String): List<FarmingPosition> {
        val tokensIds = veVeloContract.getTokenIdsForOwner(address)
        val results = veVeloStakingMarketProvider.getBlockchainGateway().readMultiCall(
            tokensIds.map { veVeloContract.lockedFn(it) }.map {
                MultiCallElement(it, veVeloStakingMarketProvider.veVelo)
            }
        )

        return results.map {
            create(
                veVeloStakingMarketProvider.getMarkets().first(),
                it[0].value as BigInteger,
                it[0].value as BigInteger, //[1] is unlock itme
            )
        }
    }
}