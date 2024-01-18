package io.defitrack

import io.defitrack.ens.adapter.rest.ENSRestController
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ENSSmokeTest {

    @Autowired
    private lateinit var ensRestController: ENSRestController

    @Test
    fun contextLoads() {
        Assertions.assertThat(ensRestController).isNotNull
    }
}