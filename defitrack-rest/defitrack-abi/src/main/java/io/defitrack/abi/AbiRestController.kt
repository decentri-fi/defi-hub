package io.defitrack.abi

import io.defitrack.abi.vo.AbiVO
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AbiRestController(private val abiService: AbiService) {

    @RequestMapping(value = ["/"], params = ["id"])
    fun getABI(@RequestParam("id") abi: String): ResponseEntity<AbiVO> = runBlocking {
        AbiVO(
            abi,
            abiService.getABI(abi)
        ).let {
            ResponseEntity.ok(it)
        }
    }
}