package io.defitrack.protocol.convex

import org.springframework.stereotype.Service

@Service
class ConvexService {

    fun providePools(): List<ConvexPool> {
        return listOf(
            ConvexPool(address = "0x3Fe65692bfCD0e6CF84cB1E7d24108E434A7587e", name = "cvxCRV Rewards"),
            ConvexPool(address = "0xCF50b810E57Ac33B91dCF525C6ddd9881B139332", name = "CVX Rewards")
        )
    }

    fun provideBooster() : String{
        return "0xf403c135812408bfbe8713b5a23a04b3d48aae31"
    }

    fun lockedRewardPool(): ConvexLockedRewardPool {
        return ConvexLockedRewardPool(
            "0xd18140b4b819b895a3dba5442f959fa44994af50",
            "CVX Locker (vICVX)"
        )
    }
}