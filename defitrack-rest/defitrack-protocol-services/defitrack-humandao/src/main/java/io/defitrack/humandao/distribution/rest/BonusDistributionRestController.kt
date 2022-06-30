package io.defitrack.humandao.distribution.rest

import io.defitrack.humandao.distribution.service.BonusDistributionService
import io.defitrack.common.network.Network
import io.defitrack.humandao.distribution.vo.BonusDistributionStatus
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class BonusDistributionRestController(private val bonusDistributionService: BonusDistributionService) {

    @GetMapping("/bonus-distribution/{address}")
    fun checkEligibility(
        @RequestParam("network") network: Network,
        @PathVariable("address") address: String
    ): ResponseEntity<BonusDistributionStatus> = runBlocking {
        ResponseEntity.ok(bonusDistributionService.getBonusDistributionStatus(network, address))
    }
}