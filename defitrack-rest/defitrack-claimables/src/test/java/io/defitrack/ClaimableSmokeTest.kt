package io.defitrack

import io.defitrack.claimables.adapters.ClaimableAggregateRestControllerDocumentation
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ClaimableSmokeTest {

    @Autowired
    private lateinit var claimableRestController: ClaimableAggregateRestControllerDocumentation

    @Test
    fun contextLoads() {
        Assertions.assertThat(claimableRestController).isNotNull
    }
}