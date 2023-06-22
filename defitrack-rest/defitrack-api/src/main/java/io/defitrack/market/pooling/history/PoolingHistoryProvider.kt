package io.defitrack.market.pooling.history

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.token.ERC20Resource
import org.springframework.beans.factory.annotation.Autowired

abstract class PoolingHistoryProvider(
    val poolingMarketProvider: PoolingMarketProvider
) {


    @Autowired
    lateinit var erC20Resource: ERC20Resource

    abstract fun historicEventExtractor(): HistoricEventExtractor

    fun getNetwork(): Network {
        return poolingMarketProvider.getNetwork()
    }
}