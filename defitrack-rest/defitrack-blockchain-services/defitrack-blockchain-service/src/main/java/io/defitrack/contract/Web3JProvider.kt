package io.defitrack.contract

import io.defitrack.evm.web3j.RateLimiterInterceptor
import io.defitrack.evm.web3j.SimpleRateLimiter
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jService
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.websocket.WebSocketClient
import org.web3j.protocol.websocket.WebSocketService
import java.net.ConnectException
import java.net.URI
import java.util.concurrent.TimeUnit

@Configuration
@ConditionalOnMissingBean(value = [Web3j::class])
class Web3JProvider(@Value("\${io.defitrack.evm.endpoint.url}") private val endpoint: String) {

    protected var webSocketClient: WebSocketClient? = null

    val logger: Logger = LoggerFactory.getLogger(Web3JProvider::class.java)

    @Scheduled(fixedRate = 20000)
    fun assureConnection() {
        try {
            webSocketClient?.let {
                if (!it.isOpen) {
                    it.reconnectBlocking()
                }
            }
        } catch (ex: Exception) {
            logger.error("Unable to reconnect to websocket")
            throw ex
        }
    }

    @Bean
    fun web3jService(): Web3jService {
        return if (endpoint.startsWith("ws")) {
            this.webSocketClient = WebSocketClient(URI.create(endpoint))
            val webSocketService = WebSocketService(webSocketClient, false)
            webSocketService.connect({

            }, {
                logger.error("An error occurred in secondary websocket", it)
                assureConnection()
            }, {
                logger.info("Websocket connection closed")
            })
            webSocketService
        } else {
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(20, TimeUnit.SECONDS)
            builder.writeTimeout(60, TimeUnit.SECONDS)
            builder.readTimeout(60, TimeUnit.SECONDS)
            builder.callTimeout(60, TimeUnit.SECONDS)
            builder.addInterceptor(
                RateLimiterInterceptor(
                    SimpleRateLimiter(10.0)
                )
            )
            HttpService(endpoint, false)
        }
    }

    @Bean
    @Throws(ConnectException::class)
    fun web3j(web3jService: Web3jService): Web3j {
        return Web3j.build(web3jService)
    }
}