package io.defitrack.protocol.mapper

import io.defitrack.company.CompanyVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolVO
import org.springframework.stereotype.Component

@Component
class ProtocolVOMapper {

    fun map(protocol: Protocol): ProtocolVO {
        return with(protocol) {
            ProtocolVO(
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