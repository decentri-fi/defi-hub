package io.defitrack.erc20.rest

import io.defitrack.erc20.adapter.rest.ERC20RestRestController
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ERC20SmokeTest {

    @Autowired
    private lateinit var erC20RestControllerImpl: ERC20RestRestController

    @Test
    fun contextLoads() {
        assertThat(erC20RestControllerImpl).isNotNull
    }
}