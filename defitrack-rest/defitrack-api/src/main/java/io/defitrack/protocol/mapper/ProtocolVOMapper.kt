package io.defitrack.protocol.mapper

import io.defitrack.protocol.CompanyVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolInformation
import org.springframework.stereotype.Component

@Component
class ProtocolVOMapper {

    fun map(protocol: Protocol): ProtocolInformation {
        return with(protocol) {
            ProtocolInformation(
                name = this.name,
                logo = this.getImage(),
                slug = this.slug,
                primitives = this.primitives,
                website = this.website,
                company = CompanyVO(
                    name = company.prettyName,
                    slug = company.slug
                )
            )
        }
    }
}