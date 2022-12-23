package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.protocol.beefy.staking.invest.BeefyStakingInvestmentPreparer
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

abstract class BeefyFarmingMarketProvider(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val vaults: List<BeefyVault>,
    private val erC20Resource: ERC20Resource,
    private val priceService: PriceResource
) : FarmingMarketProvider() {

    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> =
        withContext(Dispatchers.IO.limitedParallelism(5)) {
            vaults.map {
                async {
                    toStakingMarketElement(it)
                }
            }.awaitAll().filterNotNull()
        }

    private suspend fun toStakingMarketElement(beefyVault: BeefyVault): FarmingMarket? {
        return try {
            val contract = BeefyVaultContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
                vaultV6ABI,
                beefyVault.earnContractAddress,
                beefyVault.id
            )
            val want = erC20Resource.getTokenInformation(getNetwork(), contract.want())
            val pricePerFullShare = contract.getPricePerFullShare()
            create(
                identifier = contract.vaultId,
                name = "${contract.symbol()} Beefy Vault",
                apr = getAPY(contract),
                stakedToken = want.toFungibleToken(),
                rewardTokens = listOf(
                    want.toFungibleToken()
                ),
                marketSize = getMarketSize(want, contract),
                vaultType = "beefyVaultV6",
                balanceFetcher = PositionFetcher(
                    contract.address,
                    { user -> contract.balanceOfMethod(user) },
                    extractBalance = { result ->
                        ((result[0].value as BigInteger).times(pricePerFullShare)).dividePrecisely(
                            BigDecimal.TEN.pow(18)
                        ).toBigInteger()
                    }
                ),
                investmentPreparer = BeefyStakingInvestmentPreparer(contract, erC20Resource),
                farmType = FarmType.VAULT
            )
        } catch (ex: Exception) {
            logger.error("Unable to get beefy farm ${beefyVault.id}")
            null
        }
    }

    private suspend fun getMarketSize(
        want: TokenInformationVO,
        beefyVault: BeefyVaultContract
    ) = BigDecimal.valueOf(
        priceService.calculatePrice(
            PriceRequest(
                want.address,
                getNetwork(),
                beefyVault.balance().toBigDecimal()
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