package io.defitrack.architecture

import io.defitrack.common.network.Network
import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(OnNetworkCondition::class)
annotation class ConditionalOnNetwork(
    val value: Network
)