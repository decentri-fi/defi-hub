package io.defitrack.claimable

import io.defitrack.transaction.PreparedTransaction

class ClaimableRewardFetcher(
    val rewards: List<Reward>,
    val preparedTransaction: suspend (user: String) -> PreparedTransaction,
) {
    constructor(reward: Reward, preparedTransaction: suspend (user: String) -> PreparedTransaction) : this(
        listOf(reward),
        preparedTransaction)
}