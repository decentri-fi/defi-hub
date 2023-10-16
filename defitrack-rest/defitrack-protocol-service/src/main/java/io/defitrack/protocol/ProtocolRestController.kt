package io.defitrack.protocol

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class ProtocolRestController(
    private val companyProvider: CompaniesProvider,
) {

    @GetMapping
    fun getCompany(): List<CompanyVO> {
        return companyProvider.getCompanies().map {
            CompanyVO(
                name = it.prettyName,
                slug = it.slug
            )
        }
    }
}