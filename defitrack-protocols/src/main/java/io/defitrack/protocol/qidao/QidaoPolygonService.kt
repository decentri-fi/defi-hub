package io.defitrack.protocol.qidao

import org.springframework.stereotype.Service

@Service
class QidaoPolygonService {

    fun provideVaults(): List<String> {
        return listOf(
            "0x37131aedd3da288467b6ebe9a77c523a700e6ca1",
            "0x3fd939b017b31eaadf9ae50c7ff7fa5c0661d47c",
            "0x61167073e31b1dad85a3e531211c7b8f1e5cae72",
            "0x305f113ff78255d4f8524c8f50c7300b91b10f6a",
            "0x649aa6e6b6194250c077df4fb37c23ee6c098513",
            "0x98b5f32dd9670191568b661a3e847ed764943875",
            "0x87ee36f780ae843a78d5735867bc1c13792b7b11",
            "0x701a1824e5574b0b6b1c8da808b184a7ab7a2867")
    }

    fun farms(): List<String> {
        return listOf(
            "0x0635af5ab29fc7bba007b8cebad27b7a3d3d1958",
            "0x574fe4e8120c4da1741b5fd45584de7a5b521f0f",
            "0x07ca17da3b54683f004d388f206269ef128c2356"
        )
    }
}