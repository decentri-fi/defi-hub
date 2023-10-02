import io.defitrack.claimable.ClaimableMarket

interface ClaimableMarketProvider {

    suspend fun getClaimables(): List<ClaimableMarket>
}