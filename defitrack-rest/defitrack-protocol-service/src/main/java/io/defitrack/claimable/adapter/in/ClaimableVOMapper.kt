package io.defitrack.claimable.adapter.`in`

import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.claim.UserClaimable
import io.defitrack.claimable.adapter.`in`.rest.domain.UserClaimableVO
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.toVO
import io.defitrack.mapper.toVO
import io.defitrack.network.toVO
import io.defitrack.port.output.PriceClient
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component

@Component
class ClaimableVOMapper(
    private val priceResource: PriceClient,
    private val protocolVOMapper: ProtocolVOMapper,
) {

    suspend fun map(userClaimable: UserClaimable): UserClaimableVO {
        return with(userClaimable) {
            val amount = amount.asEth(claimableToken.decimals)
            val claimableInDollar = priceResource.calculatePrice(
                GetPriceCommand(
                    address = claimableToken.address,
                    network = network,
                    amount = amount,
                )
            )

            UserClaimableVO(
                id = id,
                name = name,
                protocol = protocolVOMapper.map(protocol),
                network = network.toVO(),
                token = claimableToken.toVO(),
                amount = amount.toDouble(),
                dollarValue = claimableInDollar,
                claimTransaction = claimTransaction?.toVO()
            )
        }
    }
}