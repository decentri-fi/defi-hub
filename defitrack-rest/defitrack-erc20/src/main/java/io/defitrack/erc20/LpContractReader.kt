package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.contract.LPTokenContract
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Service
import java.util.*

@Service
class LpContractReader(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val cache = Cache.Builder<String, LPTokenContract>().build()

    suspend fun getLP(network: Network, address: String): LPTokenContract {
        val key = "${network.name}-${address.lowercase(Locale.getDefault())}"
        return cache.get(key) {
            LPTokenContract(
                blockchainGatewayProvider.getGateway(network),
                address = address
            )
        }
    }
}