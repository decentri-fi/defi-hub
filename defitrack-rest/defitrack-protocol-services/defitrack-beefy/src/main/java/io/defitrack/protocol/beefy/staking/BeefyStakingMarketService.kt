package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.protocol.beefy.staking.invest.BeefyStakingInvestmentPreparer
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

abstract class BeefyStakingMarketService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val vaults: List<BeefyVault>,
    private val erC20Resource: ERC20Resource,
    private val priceService: PriceResource
) : StakingMarketService() {

    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarket> =
        withContext(Dispatchers.IO.limitedParallelism(5)) {
            vaults.map {
                async {
                    toStakingMarketElement(it)
                }
            }.awaitAll().filterNotNull()
        }

    private suspend fun toStakingMarketElement(beefyVault: BeefyVault): StakingMarket? {
        return try {
            val contract = BeefyVaultContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
                vaultV6ABI,
                beefyVault.earnContractAddress,
                beefyVault.id
            )
            val want = erC20Resource.getTokenInformation(getNetwork(), contract.want)
            StakingMarket(
                id = contract.vaultId,
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${contract.symbol} Beefy Vault",
                apr = getAPY(contract),
                stakedToken = want.toFungibleToken(),
                rewardTokens = listOf(
                    want.toFungibleToken()
                ),
                contractAddress = contract.address,
                marketSize = getMarketSize(want, contract),
                vaultType = "beefyVaultV6",
                balanceFetcher = StakingMarketBalanceFetcher(
                    contract.address,
                    { user -> contract.balanceOfMethod(user) }
                ),
                underlyingBalanceFetcher = StakingMarketBalanceFetcher(
                    contract.address,
                    { user -> contract.balanceOfMethod(user) },
                    extractBalance = { result ->
                        ((result[0].value as BigInteger).times(contract.getPricePerFullShare)).dividePrecisely(
                            BigDecimal.TEN.pow(18)
                        ).toBigInteger()
                    }
                ),
                investmentPreparer = BeefyStakingInvestmentPreparer(contract, erC20Resource)
            )
        } catch (ex: Exception) {
            null
        }
    }

    private suspend fun getMarketSize(
        want: TokenInformation,
        beefyVault: BeefyVaultContract
    ) = BigDecimal.valueOf(
        priceService.calculatePrice(
            PriceRequest(
                want.address,
                getNetwork(),
                beefyVault.balance.toBigDecimal()
                    .divide(BigDecimal.TEN.pow(want.decimals), 18, RoundingMode.HALF_UP),
                want.type
            )
        )
    )

    private fun getAPY(beefyVault: BeefyVaultContract): BigDecimal {
        return try {
            (beefyAPYService.getAPYS().getOrDefault(beefyVault.vaultId, null)) ?: BigDecimal.ZERO
        } catch (ex: Exception) {
            BigDecimal.ZERO
        }
    }

}