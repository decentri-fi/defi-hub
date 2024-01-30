package io.defitrack.architecture.conditional

import io.defitrack.common.network.Network
import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(OnCompanyCondition::class)
annotation class ConditionalOnNetwork(
    val value: Network
)