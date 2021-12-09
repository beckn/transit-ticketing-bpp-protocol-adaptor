package transit.ticketing.bpp.protocol.message.entities

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default

data class ProtocolPriceDao @Default constructor(
    val currency: String,
    val value: String? = null,
)
