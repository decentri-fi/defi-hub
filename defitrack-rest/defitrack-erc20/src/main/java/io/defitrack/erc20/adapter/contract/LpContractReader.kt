package io.defitrack.erc20.adapter.contract

import io.defitrack.common.network.Network
import io.defitrack.erc20.port.output.ReadLPPort
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.LPTokenContract
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import java.util.*

@Component
private class LpContractReader(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : ReadLPPort {

    private val cache = Cache.Builder<String, LPTokenContract>().build()

    override suspend fun getLP(network: Network, address: String): LPTokenContract {
        val key = "${network.name}-${address.lowercase(Locale.getDefault())}"
        return cache.get(key) {
            with(blockchainGatewayProvider.getGateway(network)) {
                LPTokenContract(address)
            }
        }
    }
}