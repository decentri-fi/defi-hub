package io.defitrack.staking.domain

import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.transaction.PreparedTransaction

interface InvestmentPreparer {
    suspend fun prepare(prepareInvestmentCommand: PrepareInvestmentCommand) : List<PreparedTransaction>
}