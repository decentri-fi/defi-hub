package io.defitrack.erc20.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ERC20SmokeTest {

    @Autowired
    private lateinit var erC20RestController: ERC20RestController

    @Test
    fun contextLoads() {
        assertThat(erC20RestController).isNotNull
    }
}