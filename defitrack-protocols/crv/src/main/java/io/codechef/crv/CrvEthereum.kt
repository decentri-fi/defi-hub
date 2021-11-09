package io.defitrack.crv

import io.defitrack.ethereum.DefitrackEthereum
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [DefitrackEthereum::class])
class CrvEthereum {
}