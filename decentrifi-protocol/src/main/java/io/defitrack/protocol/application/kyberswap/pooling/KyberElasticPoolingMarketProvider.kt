package io.defitrack.protocol.application.kyberswap.pooling

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kyberswap.contract.KyberswapElasticContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.KYBER_SWAP)
class KyberElasticPoolingMarketProvider : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val contract = KyberswapElasticContract(
            getBlockchainGateway(),
            "0xb85ebe2e4ea27526f817ff33fb55fb240057c03f"
        )

        return contract.allPairs().parMapNotNull(concurrency = 12) { poolInfo ->
            catch {
                createPoolingMarket(poolInfo)
            }.mapLeft {
                logger.error("Unable to get pooling market {}: {}", poolInfo.address, it.message)
                null
            }.getOrNull()
        }
    }

    private suspend fun createPoolingMarket(poolInfo: KyberswapElasticContract.PoolInfo): PoolingMarket {
        val poolingToken = getToken(poolInfo.address)
        val tokens = poolingToken.underlyingTokens

        val breakdown = refreshable {
            fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address)
        }
        return create(
            identifier = poolInfo.address,
            address = poolInfo.address,
            name = poolingToken.name,
            breakdown = breakdown,
            symbol = poolingToken.symbol,
            tokens = poolingToken.underlyingTokens,
            totalSupply = refreshable {
                getToken(poolInfo.address).totalDecimalSupply()
            },
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.KYBER_SWAP
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}