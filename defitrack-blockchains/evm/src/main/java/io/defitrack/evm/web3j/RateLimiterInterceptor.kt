package io.defitrack.evm.web3j

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class RateLimiterInterceptor(
    private val simpleRateLimiter: SimpleRateLimiter
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
        simpleRateLimiter.acquire()
        chain.proceed(chain.request())
    }
}