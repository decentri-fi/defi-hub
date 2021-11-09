package io.defitrack.belt

import io.defitrack.bsc.DefitrackBsc
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [DefitrackBsc::class])
class BeltBsc {
}