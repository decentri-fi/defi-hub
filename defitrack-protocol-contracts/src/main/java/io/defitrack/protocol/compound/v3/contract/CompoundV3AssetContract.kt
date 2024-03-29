package io.defitrack.protocol.compound.v3.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint8
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.StaticStruct
import org.web3j.abi.datatypes.generated.Uint128
import org.web3j.abi.datatypes.generated.Uint64
import org.web3j.abi.datatypes.generated.Uint8
import java.math.BigInteger

context(BlockchainGateway)
class CompoundV3AssetContract(address: String) : ERC20Contract(address) {

    val baseToken = constant<String>("baseToken", TypeUtils.address())
    val numAssets = constant<BigInteger>("numAssets", uint256())

    suspend fun borrowBalanceOf(): BigInteger {
        return read(
            "borrowBalanceOf",
            outputs = listOf(TypeUtils.uint256())
        )[0].value as BigInteger
    }

    suspend fun getAssetInfos(): List<AssetInfo> {
        val functions = (0 until numAssets.await().toInt()).map { assetIndex ->
            createFunction(
                "getAssetInfo",
                inputs = listOf(assetIndex.toBigInteger().toUint8()),
                outputs = listOf(
                    object : TypeReference<AssetInfo>() {},
                ),
            )
        }

        val results = this.readMultiCall(functions)

        return results.map {
            it.data[0] as AssetInfo
        }
    }

    class AssetInfo : StaticStruct {
        val offset: BigInteger
        val asset: String
        val priceFeed: String
        val scale: BigInteger
        val borrowLiquidationFactor: BigInteger
        val liquidateCollateralFactor: BigInteger
        val liquidationFactor: BigInteger
        val supplyCap: BigInteger

        constructor(
            _offset: BigInteger,
            _asset: String,
            _priceFeed: String,
            _scale: BigInteger,
            _borrowLiquidationFactor: BigInteger,
            _liquidateCollateralFacotr: BigInteger,
            _liquidationFactor: BigInteger,
            _supplyCap: BigInteger
        ) : super(
            Uint8(_offset),
            Address(_asset),
            Address(_priceFeed),
            Uint64(_scale),
            Uint64(_borrowLiquidationFactor),
            Uint64(_liquidateCollateralFacotr),
            Uint64(_liquidationFactor),
            Uint128(_supplyCap)
        ) {
            offset = _offset
            asset = _asset
            priceFeed = _priceFeed
            scale = _scale
            borrowLiquidationFactor = _borrowLiquidationFactor
            liquidateCollateralFactor = _liquidateCollateralFacotr
            liquidationFactor = _liquidationFactor
            supplyCap = _supplyCap
        }

        constructor(
            _offset: Uint8,
            _asset: Address,
            _priceFeed: Address,
            _scale: Uint64,
            _borrowLiquidationFactor: Uint64,
            _liquidateCollateralFacotr: Uint64,
            _liquidationFactor: Uint64,
            _supplyCap: Uint128
        ) : super(
            _offset,
            _asset,
            _priceFeed,
            _scale,
            _borrowLiquidationFactor,
            _liquidateCollateralFacotr,
            _liquidationFactor,
            _supplyCap
        ) {
            offset = _offset.value
            asset = _asset.value
            priceFeed = _priceFeed.value
            scale = _scale.value
            borrowLiquidationFactor = _borrowLiquidationFactor.value
            liquidateCollateralFactor = _liquidateCollateralFacotr.value
            liquidationFactor = _liquidationFactor.value
            supplyCap = _supplyCap.value
        }
    }

}