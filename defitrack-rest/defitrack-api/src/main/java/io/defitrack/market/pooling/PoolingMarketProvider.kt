package io.defitrack.market.pooling

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.invest.MarketProvider
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.token.ERC20Resource
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.math.BigInteger

abstract class PoolingMarketProvider() : MarketProvider<PoolingMarket>() {


}