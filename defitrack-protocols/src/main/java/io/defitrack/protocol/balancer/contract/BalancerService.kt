package io.defitrack.protocol.balancer.contract

import com.google.gson.JsonParser
import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.evm.contract.EventUtils.extract
import io.defitrack.protocol.balancer.contract.BalancerPoolFactoryContract.Companion.POOL_CREATED_EVENT
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionReturnDecoder
import java.math.BigInteger
import kotlin.time.Duration.Companion.days

@Component
class BalancerService(private val blockchainGatewayProvider: BlockchainGatewayProvider) {

    val configs = mapOf(
        Network.POLYGON to BalancerConfig(
            listOf(
                "0x136fd06fa01ecf624c7f2b3cb15742c1339dc2c4",
                "0xAB2372275809E15198A7968C7f324053867cdB0C",
                "0x8e9aa87e45e92bad84d5f8dd1bff34fb92637de9",
                "0x7bc6C0E73EDAa66eF3F6E2f27b0EE8661834c6C9",
                "0x6Ab5549bBd766A43aFb687776ad8466F8b42f777",
                "0x5C5fCf8fBd4cd563cED27e7D066b88ee20E1867A",
                "0xB8Dfa4fd0F083de2B7EDc0D5eeD5E684e54bA45D",
                "0xFc8a407Bba312ac761D8BFe04CE1201904842B76",
                "0x0b576c1245F479506e7C8bbc4dB4db07C1CD31F9"
            ),
            "0"
        ),
        Network.POLYGON_ZKEVM to BalancerConfig(
            listOf(
                "0x4b7b369989e613ff2C65768B7Cf930cC927F901E",
                "0x956CCab09898C0AF2aCa5e6C229c3aD4E93d9288",
                "0x6B1Da720Be2D11d95177ccFc40A917c2688f396c",
                "0x687b8C9b41E01Be8B591725fac5d5f52D0564d79",
                "0xaf779e58dafb4307b998C7b3C9D3f788DFc80632",
                "0x03F3Fb107e74F2EAC9358862E91ad3c692712054",
                "0x44d33798dddCdAbc93Fe6a40C80588033Dc502d3",
            ),
            "0"
        ),
        Network.OPTIMISM to BalancerConfig(
            listOf(
                "0x7396f99B48e7436b152427bfA3DD6Aa8C7C6d05B",
                "0x1802953277FD955f9a254B80Aa0582f193cF1d77",
                "0x7ADbdabaA80F654568421887c12F09E0C7BD9629",
                "0x4C32a8a8fDa4E24139B51b456B42290f51d6A1c4",
                "0x230a59F4d9ADc147480f03B0D3fFfeCd56c3289a",
                "0x19DFEF0a828EEC0c85FbB335aa65437417390b85",
                "0x043A2daD730d585C44FB79D2614F295D2d625412"
            ),
            "0"
        ),
        Network.BASE to BalancerConfig(
            listOf(
                "0x687b8C9b41E01Be8B591725fac5d5f52D0564d79",
                "0x8df317a729fcaA260306d7de28888932cb579b88",
                "0x161f4014C27773840ccb4EC1957113e6DD028846",
                "0x9Dd32684176638D977883448A4c914311c07bd62",
                "0x9a62C91626d39D0216b3959112f9D4678E20134d",
                "0x4C32a8a8fDa4E24139B51b456B42290f51d6A1c4",
                "0x44d33798dddCdAbc93Fe6a40C80588033Dc502d3"
            ),
            "0"
        ),
        Network.POLYGON_ZKEVM to BalancerConfig(
            listOf(
                "0x4b7b369989e613ff2C65768B7Cf930cC927F901E",
                "0x8eA89804145c007e7D226001A96955ad53836087",
                "0x6B1Da720Be2D11d95177ccFc40A917c2688f396c",
                "0x687b8C9b41E01Be8B591725fac5d5f52D0564d79",
                "0xaf779e58dafb4307b998C7b3C9D3f788DFc80632",
                "0x03F3Fb107e74F2EAC9358862E91ad3c692712054",
                "0x44d33798dddCdAbc93Fe6a40C80588033Dc502d3"
            ),
            "0"
        ),
        Network.ARBITRUM to BalancerConfig(
            listOf(
                "0x7396f99B48e7436b152427bfA3DD6Aa8C7C6d05B",
                "0x1c99324edc771c82a0dccb780cc7dda0045e50e7",
                "0x2498A2B0d6462d2260EAC50aE1C3e03F4829BA95",
                "0x7ADbdabaA80F654568421887c12F09E0C7BD9629",
                "0x8eA89804145c007e7D226001A96955ad53836087",
                "0xc7E5ED1054A24Ef31D827E6F86caA58B3Bc168d7",
                "0x19DFEF0a828EEC0c85FbB335aa65437417390b85"
            ),
            "0"
        ),
        Network.ETHEREUM to BalancerConfig(
            listOf(
                "0x67d27634e44793fe63c467035e31ea8635117cd4", //meta stable pool factory
                "0xdba127fBc23fb20F5929C546af220A991b5C6e01",
                "0xE061bF85648e9FA7b59394668CfEef980aEc4c66",
                "0x67A25ca2350Ebf4a0C475cA74C257C94a373b828",
                "0xf9ac7B9dF2b3454E841110CcE5550bD5AC6f875F",
                "0x5dd94da3644ddd055fcf6b3e1aa310bb7801eb8b", //weighted pool factory,
                "0xc66ba2b6595d3613ccab350c886ace23866ede24", //stable pool factory,
                "0x0b576c1245F479506e7C8bbc4dB4db07C1CD31F9",
                "0xfADa0f4547AB2de89D1304A668C39B3E09Aa7c76",
                "0x5F43FBa61f63Fa6bFF101a0A0458cEA917f6B347",
                "0xBF904F9F340745B4f0c4702c7B6Ab1e808eA6b93",
                "0x4E11AEec21baF1660b1a46472963cB3DA7811C89",
                "0x897888115Ada5773E02aA29F775430BFB5F34c51",
                "0x5F5222Ffa40F2AEd6380D022184D6ea67C776eE0",
                "0x39A79EB449Fc05C92c39aA6f0e9BfaC03BE8dE5B",
                "0x813EE7a840CE909E7Fea2117A44a90b8063bd4fd",
                "0xDB8d758BCb971e482B2C45f7F8a7740283A1bd3A",

                ),
            "12703126"
        )
    )

    val cache = Cache.Builder<Network, List<String>>().expireAfterWrite(1.days).build()

    suspend fun getPools(network: Network): List<String> {
        return cache.get(network) {
            val config = configs[network] ?: return@get emptyList()
            val logs = blockchainGatewayProvider.getGateway(network).getEventsAsEthLog(
                GetEventLogsCommand(
                    addresses = config.factories,
                    topic = EventEncoder.encode(POOL_CREATED_EVENT),
                    fromBlock = BigInteger(config.earliestBlock, 10),
                )
            )

            logs.map {
               POOL_CREATED_EVENT.extract<String>(it.get(), true, 0)
            }
        }
    }

    data class BalancerConfig(
        val factories: List<String>,
        val earliestBlock: String
    )
}