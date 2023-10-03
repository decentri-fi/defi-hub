package io.defitrack.protocol

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class ProtocolRestController(
    private val companyProvider: CompanyProvider,
) {

    @GetMapping
    fun getCompany(): Company {
        return companyProvider.getCompany()
    }
}