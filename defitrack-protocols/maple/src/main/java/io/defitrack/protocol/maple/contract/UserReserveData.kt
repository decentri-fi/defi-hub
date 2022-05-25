package io.defitrack.protocol.maple.contract

import java.math.BigInteger

class UserReserveData(
  val id: String
  val pool: Pool
  val poolTokenBalance: String
  val poolTokensStaked: String
  val claimableInterest: String
  val interestEarned: String
  val claimableLendingReward: String
  val rewardPaid: String
  val custodyAllowance: String
  val depositDate: String
  val withdrawCooldown: String
  val withdrawStatus: String
  val stake: String
  val stakeLockerTokensStaked: String
  val claimableFees: String
  val feesEarned: String
  val claimableStakingReward: String
  val stakeRewardPaid: String
  val stakeCustodyAllowance: String
  val stakeDate: String
  val unstakeCooldown: String
  val transaction: Transaction
)
