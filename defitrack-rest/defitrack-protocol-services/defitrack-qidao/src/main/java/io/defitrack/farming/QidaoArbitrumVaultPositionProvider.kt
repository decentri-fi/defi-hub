package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.market.lending.LendingPositionService
import io.defitrack.market.lending.domain.LendingPosition
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.QidaoVaultContract
import org.springframework.stereotype.Service

@Service
class QidaoArbitrumVaultPositionProvider(
    private val qiDaoArbitrumVaultProvider: QiDaoArbitrumVaultProvider
) : LendingPositionService() {
    override suspend fun getLendings(user: String): List<LendingPosition> {
        return qiDaoArbitrumVaultProvider.getMarkets().flatMap { market ->
            val qidaoVaultContract = market.metadata["vaultContract"] as QidaoVaultContract
            val userVaults = qidaoVaultContract.getVaults(user)

            userVaults.map {
                LendingPosition(
                    qidaoVaultContract.vaultCollateral(it.toBigInteger()),
                    market,
                )
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.QIDAO
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}