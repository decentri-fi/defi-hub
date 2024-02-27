package io.defitrack.architecture

import io.defitrack.protocol.Company
import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(OnCompanyCondition::class)
annotation class ConditionalOnCompany(
    val value: Company
)