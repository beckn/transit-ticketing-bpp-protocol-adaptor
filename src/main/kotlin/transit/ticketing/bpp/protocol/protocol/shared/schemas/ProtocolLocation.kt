package transit.ticketing.bpp.protocol.protocol.shared.schemas

data class ProtocolLocation @Default constructor(
    val id: String? = null,
    val descriptor: ProtocolDescriptor? = null,
    val gps: String? = null,
    val stationCode: String? = null,
    val city: ProtocolCity? = null,
    val country: ProtocolCountry? = null,
    val circle: ProtocolCircle? = null,
    val polygon: String? = null,
    val `3dspace`: String? = null,
//    val address: ProtocolAddress? = null,
    )

data class ProtocolCity @Default constructor(
    val name: String? = null,
    val code: String? = null
)

data class ProtocolCountry @Default constructor(
    val name: String? = null,
    val code: String? = null
)

data class ProtocolCircle @Default constructor(
    val radius: ProtocolScalar? = null
)

data class ProtocolScalar @Default constructor(
    val value: java.math.BigDecimal,
    val unit: String,
    val type: Type? = null,
    val estimatedValue: java.math.BigDecimal? = null,
    val computedValue: java.math.BigDecimal? = null,
    val range: ProtocolScalarRange? = null
) {

    enum class Type(val value: String) {
        CONSTANT("CONSTANT"),
        VARIABLE("VARIABLE");
    }
}

data class ProtocolScalarRange @Default constructor(
    val min: java.math.BigDecimal? = null,
    val max: java.math.BigDecimal? = null
)