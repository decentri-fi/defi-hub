package io.defitrack.crv

import io.defitrack.matic.DefitrackPolygon
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [DefitrackPolygon::class])
class CrvPolygon {
}