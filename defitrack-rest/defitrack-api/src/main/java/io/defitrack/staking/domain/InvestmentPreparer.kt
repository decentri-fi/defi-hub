package io.defitrack.staking.domain

import io.defitrack.staking.command.PrepareInvestmentCommand
import io.defitrack.transaction.PreparedTransaction

class InvestmentPreparer(
    val prepare: (prepareInvestment: PrepareInvestmentCommand) -> List<PreparedTransaction>
)