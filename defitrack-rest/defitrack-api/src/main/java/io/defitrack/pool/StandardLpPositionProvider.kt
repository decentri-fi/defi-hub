package io.defitrack.pool

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.BigInteger

abstract class StandardLpPositionProvider(
    private val poolingMarketService: PoolingMarketService,
    private val erC20Resource: ERC20Resource,
) : UserPoolingService() {
    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val markets = poolingMarketService.getPoolingMarkets()

        return erC20Resource.getBalancesFor(
            address,
            markets.map { it.address },
            getNetwork()
        )
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ONE) {
                    val pool = markets[index]

                    val tokenInfo = erC20Resource.getTokenInformation(getNetwork(), pool.address)

                    PoolingElement(
                        lpAddress = pool.address,
                        amount = balance.toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(tokenInfo.decimals)),
                        name = pool.tokens.joinToString("/") { it.symbol } + " LP",
                        symbol = tokenInfo.symbol,
                        network = getNetwork(),
                        protocol = getProtocol(),
                        tokenType = TokenType.HOP,
                        id = pool.id,
                        apr = pool.apr,
                        marketSize = pool.marketSize
                    )
                } else {
                    null
                }
            }.filterNotNull()
    }
}