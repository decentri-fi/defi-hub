package io.defitrack.protocol.application.stader

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stader.MaticXStakingContract
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.STADER)
@ConditionalOnNetwork(Network.POLYGON)
class StaderLiquidMaticMarketProvider : FarmingMarketProvider() {

    val staderLiquidMaticAddress = "0xfa68fb4628dff1028cfec22b4162fccd0d45efb6"
    val stakerAddress = "0xfd225c9e6601c9d38d8f98d8731bf59efcf8c0e3"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val token = getToken(staderLiquidMaticAddress)
        val stakingContract = MaticXStakingContract(getBlockchainGateway(), stakerAddress)

        return create(
            name = token.name,
            identifier = token.address,
            stakedToken = getToken("0x0"),
            rewardToken = getToken("0x0"),
            positionFetcher = PositionFetcher(
                { user ->
                    ContractCall(
                        ERC20Contract.balanceOf(user),
                        getNetwork(),
                        token.address,
                    )
                }
            ) { retVal ->
                val result = retVal[0].value as BigInteger
                if (result > BigInteger.ZERO) {
                    Position(stakingContract.convertMaticXToMatic(result), result)
                } else Position.ZERO
            },
            token = token,
            type = "stader.staked-matic"
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.STADER
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}