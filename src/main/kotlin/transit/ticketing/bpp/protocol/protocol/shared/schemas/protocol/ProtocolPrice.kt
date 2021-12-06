package transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol

data class ProtocolPrice @Default constructor(
    val currency: String,
    val value: String? = null,
)
