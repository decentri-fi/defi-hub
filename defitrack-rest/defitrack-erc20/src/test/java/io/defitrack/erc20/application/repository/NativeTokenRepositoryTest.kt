package io.defitrack.erc20.application.repository

import io.defitrack.common.network.Network
import io.defitrack.erc20.application.LogoGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class NativeTokenRepositoryTest {

    val logoGenerator = mock(LogoGenerator::class.java)

    val nativeTokenRepository = NativeTokenRepository(logoGenerator)

    @Test
    fun `all networks should have a token`() {
        Network.entries.forEach {
            val nativeToken = nativeTokenRepository.getNativeToken(it)
            assertThat(nativeToken).isNotNull
        }
    }

}