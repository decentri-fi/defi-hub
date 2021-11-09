package io.defitrack.belt

import org.springframework.stereotype.Service

@Service
class BeltService {
    fun provideMasterBeltContracts(): List<String> {
        return listOf("0xD4BbC80b9B102b77B21A06cb77E954049605E6c1")
    }
}