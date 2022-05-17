package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class BeefyPolygonStakingMarketService(
    contractAccessorGateway: ContractAccessorGateway,
    abiResource: ABIResource,
    beefyAPYService: BeefyAPYService,
    beefyService: BeefyService,
    erC20Resource: ERC20Resource,
    priceService: PriceResource
) : BeefyStakingMarketService(
    contractAccessorGateway,
    abiResource,
    beefyAPYService,
    beefyService.beefyPolygonVaults,
    erC20Resource,
    priceService
) {

    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}