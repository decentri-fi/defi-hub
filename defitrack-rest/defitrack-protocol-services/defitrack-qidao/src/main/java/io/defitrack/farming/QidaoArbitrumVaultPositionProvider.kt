package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.market.lending.LendingPositionProvider
import io.defitrack.market.lending.domain.LendingPosition
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.QidaoVaultContract

//TODO: fix this vault
class QidaoArbitrumVaultPositionProvider(
    private val qiDaoArbitrumVaultProvider: QiDaoArbitrumVaultProvider
) : LendingPositionProvider() {
    override suspend fun getLendings(user: String): List<LendingPosition> {
        return qiDaoArbitrumVaultProvider.getMarkets().flatMap { market ->
            val qidaoVaultContract = market.metadata["vaultContract"] as QidaoVaultContract
            val userVaults = qidaoVaultContract.getVaults(user)

            userVaults.map {
                val collateral = qidaoVaultContract.vaultCollateral(it.toBigInteger())
                LendingPosition(
                    collateral,
                    collateral,
                    market,
                )
            }
        }
    }
}