package io.defitrack.claimable.mapper

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableVO
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component

@Component
class ClaimableVOMapper(
    private val priceResource: PriceResource,
    private val protocolVOMapper: ProtocolVOMapper
) {

    suspend fun map(claimable: Claimable): ClaimableVO {
        return with(claimable) {
            val amount = amount.asEth(claimableTokens.firstOrNull()?.decimals ?: 18)
            val claimableInDollar = priceResource.calculatePrice(
                PriceRequest(
                    address = claimableTokens.first().address,
                    network = network,
                    amount = amount,
                    type = claimableTokens.first().type
                )
            )

            ClaimableVO(
                id = id,
                name = name,
                type = type,
                protocol = protocolVOMapper.map(protocol),
                network = network.toVO(),
                token = claimableTokens.first(),
                amount = amount.toDouble(),
                dollarValue = claimableInDollar,
                claimTransaction = claimTransaction
            )
        }
    }
}