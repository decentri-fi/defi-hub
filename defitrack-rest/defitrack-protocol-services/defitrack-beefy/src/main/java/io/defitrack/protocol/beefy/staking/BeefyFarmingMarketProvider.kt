package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.protocol.beefy.staking.invest.BeefyStakingInvestmentPreparer
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

abstract class BeefyFarmingMarketProvider(
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val vaults: List<BeefyVault>,
    private val priceService: PriceResource
) : FarmingMarketProvider() {

    val vaultV6ABI by lazy {
        runBlocking {
            abiResource.getABI("beefy/VaultV6.json")
        }
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val semaphore = Semaphore(10)
        vaults.map {
            async {
                semaphore.withPermit {
                    toStakingMarketElement(it)
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun toStakingMarketElement(beefyVault: BeefyVault): FarmingMarket? {
        return try {
            val contract = BeefyVaultContract(
                getBlockchainGateway(),
                vaultV6ABI,
                beefyVault.earnContractAddress,
                beefyVault.id
            )
            val want = getToken(contract.want())
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
                        val balance = result[0].value as BigInteger
                        Position(
                            (balance.times(pricePerFullShare)).dividePrecisely(
                                BigDecimal.TEN.pow(18)
                            ).toBigInteger(),
                            balance,
                        )
                    }
                ),
                investmentPreparer = BeefyStakingInvestmentPreparer(contract, getERC20Resource()),
                farmType = ContractType.YIELD_OPTIMIZING_AUTOCOMPOUNDER,
                exitPositionPreparer = prepareExit {
                    PreparedTransaction(
                        getNetwork().toVO(),
                        contract.fullExitFunction(),
                        contract.address,
                        from = it.user
                    )
                }
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