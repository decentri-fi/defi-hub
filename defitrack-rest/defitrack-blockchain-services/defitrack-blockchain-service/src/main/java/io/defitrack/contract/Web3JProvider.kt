package io.defitrack.contract

import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jService
import org.web3j.protocol.http.HttpService
import java.net.ConnectException
import java.util.concurrent.TimeUnit

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(value = [Web3j::class])
class Web3JProvider(@Value("\${io.defitrack.evm.endpoint.url}") private val endpoint: String) {

    @Bean
    fun web3jService(): Web3jService {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(20, TimeUnit.SECONDS)
        builder.writeTimeout(60, TimeUnit.SECONDS)
        builder.readTimeout(60, TimeUnit.SECONDS)
        builder.callTimeout(60, TimeUnit.SECONDS)
        return HttpService(endpoint, false)
    }

    @Bean
    @Throws(ConnectException::class)
    fun web3j(web3jService: Web3jService): Web3j {
        return Web3j.build(web3jService)
    }
}