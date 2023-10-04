package io.defitrack.protocol.qidao.farming

import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingPositionProvider
import io.defitrack.market.lending.domain.LendingPosition
import io.defitrack.protocol.Company
import io.defitrack.protocol.qidao.contract.QidaoVaultContract

//TODO: fix this vault
@ConditionalOnCompany(Company.QIDAO)
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