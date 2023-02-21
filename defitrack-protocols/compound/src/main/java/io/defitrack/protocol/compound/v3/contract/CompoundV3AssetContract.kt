package io.defitrack.protocol.compound.v3.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.toUint8
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.StaticStruct
import org.web3j.abi.datatypes.generated.Uint128
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint64
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.tuples.Tuple
import org.web3j.tuples.generated.Tuple1
import org.web3j.tuples.generated.Tuple8
import java.math.BigInteger

class CompoundV3AssetContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {

    suspend fun baseToken(): String {
        return readWithoutAbi(
            "baseToken",
            outputs = listOf(TypeUtils.address())
        )[0].value as String
    }

    suspend fun borrowBalanceOf(): BigInteger {
        return readWithoutAbi(
            "borrowBalanceOf",
            outputs = listOf(TypeUtils.uint256())
        )[0].value as BigInteger
    }

    suspend fun numAssets(): BigInteger {
        return readWithoutAbi(
            "numAssets",
            outputs = listOf(TypeUtils.uint256())
        )[0].value as BigInteger
    }

    suspend fun getAssetInfos(): List<AssetInfo> {
        val multicalls = (0 until numAssets().toInt()).map { assetIndex ->
            MultiCallElement(
                createFunction(
                    "getAssetInfo",
                    inputs = listOf(assetIndex.toBigInteger().toUint8()),
                    outputs = listOf(
                        object: TypeReference<AssetInfo>() {},
                    ),
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )

        return results.map {
            it[0] as AssetInfo
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