package io.defitrack.protocol.velodrome.staking

import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingPositionProvider
import io.defitrack.market.domain.farming.FarmingPosition
import io.defitrack.protocol.Company
import io.defitrack.protocol.velodrome.contract.VeVeloContract
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.VELODROME)
class VeVeloStakingPositionProvider(
    private val veVeloStakingMarketProvider: VeVeloStakingMarketProvider
) : FarmingPositionProvider() {


    override suspend fun getStakings(protocol: String, address: String): List<FarmingPosition> {
        val contract = with(veVeloStakingMarketProvider.getBlockchainGateway()) { VeVeloContract(veVeloStakingMarketProvider.veVelo) }
        val tokensIds = contract.getTokenIdsForOwner(address)
        val results = veVeloStakingMarketProvider.getBlockchainGateway().readMultiCall(
            tokensIds.map { contract.lockedFn(it) }
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