package io.defitrack.staking.domain

import io.defitrack.staking.command.PrepareInvestmentCommand
import io.defitrack.transaction.PreparedTransaction

interface InvestmentPreparer {
    suspend fun prepare(prepareInvestmentCommand: PrepareInvestmentCommand) : List<PreparedTransaction>
}