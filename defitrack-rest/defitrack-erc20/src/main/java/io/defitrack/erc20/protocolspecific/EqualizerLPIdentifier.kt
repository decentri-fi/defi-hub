package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.equalizer.EqualizerService
import io.defitrack.protocol.equalizer.EqualizerVoter
import io.defitrack.protocol.velodrome.contract.PoolFactoryContract
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days

@Component
class EqualizerLPIdentifier(
    private val equalizerService: EqualizerService,
    lpContractReader: LpContractReader,
) : DefaultLpIdentifier(
    Protocol.EQUALIZER, TokenType.EQUALIZER, lpContractReader
) {

    val cache = Cache.Builder<String, List<String>>().expireAfterWrite(1.days).build()

    suspend fun getPools() = cache.get("all") {
        equalizerService.pools.await().map(String::lowercase)
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.network == Network.BASE && getPools().contains(token.address.lowercase())
    }
}