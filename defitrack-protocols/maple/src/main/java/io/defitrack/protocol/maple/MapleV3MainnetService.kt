package io.defitrack.protocol.maple

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.maple.domain.MapleReserve
import io.defitrack.protocol.maple.domain.UserReserve
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
clas@Component
class MapleV2MainnetService(
s MapleV2MainnetService(
    graphGatewayProvider: TheGraphGatewayProvider,
    private val objectMapper: ObjectMapper,
) {
    suspend fun getUserReserveData(user: String): List<UserReserveData> {
        val query = """
           { 
              userReserveData(where: {user: "$user"}) {
                reserve {
                  id
                  pool
                  poolTokenBalance
                  poolTokensStaked
                  claimableInterest
                  interestEarned
                  claimableLendingReward
                  rewardPaid
                  custodyAllowance
                  depositDate
                  withdrawCooldown
                  withdrawStatus
                  stake
                  stakeLockerTokensStaked
                  claimableFees
                  feesEarned
                  claimableStakingReward
                  stakeRewardPaid
                  stakeCustodyAllowance
                  stakeDate
                  unstakeCooldown
                  transaction
                }                  
              }
            }
        """.trimIndent()
    }

    suspend fun getPoolPositions(): List<PoolPositions> {
        val query = """
            {
              reserves {
                  id
                  pool
                  poolTokenBalance
                  poolTokensStaked
                  claimableInterest
                  interestEarned
                  claimableLendingReward
                  rewardPaid
                  custodyAllowance
                  depositDate
                  withdrawCooldown
                  withdrawStatus
                  stake
                  stakeLockerTokensStaked
                  claimableFees
                  feesEarned
                  claimableStakingReward
                  stakeRewardPaid
                  stakeCustodyAllowance
                  stakeDate
                  unstakeCooldown
                  transaction
              }
            }
        """.trimIndent()
    }
}
