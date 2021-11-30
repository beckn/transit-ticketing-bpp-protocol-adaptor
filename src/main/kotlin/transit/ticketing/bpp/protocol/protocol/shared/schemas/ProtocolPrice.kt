package transit.ticketing.bpp.protocol.protocol.shared.schemas

data class ProtocolPrice @Default constructor(
    val currency: String,
    val value: String? = null,
)
