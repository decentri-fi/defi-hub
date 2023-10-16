package io.defitrack.conditional

import io.defitrack.protocol.Company
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(OnCompanyCondition::class)
annotation class ConditionalOnCompany(
    val value: Company
)