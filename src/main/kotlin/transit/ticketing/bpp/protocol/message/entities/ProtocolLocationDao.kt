package transit.ticketing.bpp.protocol.message.entities

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default

data class ProtocolLocationDao @Default constructor(
    val id: String? = null,
    val descriptor: ProtocolDescriptorDao? = null,
    val gps: String? = null,
    val stationCode: String? = null,
    val city: ProtocolCityDao? = null,
    val country: ProtocolCountryDao? = null,
    val circle: ProtocolCircleDao? = null,
    val polygon: String? = null,
    val `3dspace`: String? = null,
//    val address: ProtocolAddress? = null,
    )

data class ProtocolCityDao @Default constructor(
    val name: String? = null,
    val code: String? = null
)

data class ProtocolCountryDao @Default constructor(
    val name: String? = null,
    val code: String? = null
)

data class ProtocolCircleDao @Default constructor(
    val radius: ProtocolScalarDao? = null
)

data class ProtocolScalarDao @Default constructor(
    val value: java.math.BigDecimal,
    val unit: String,
    val type: Type? = null,
    val estimatedValue: java.math.BigDecimal? = null,
    val computedValue: java.math.BigDecimal? = null,
    val range: ProtocolScalarRangeDao? = null
) {

    enum class Type(val value: String) {
        CONSTANT("CONSTANT"),
        VARIABLE("VARIABLE");
    }
}

data class ProtocolScalarRangeDao @Default constructor(
    val min: java.math.BigDecimal? = null,
    val max: java.math.BigDecimal? = null
)