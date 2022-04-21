package transit.ticketing.bpp.protocol.message.entities

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default

data class ProtocolItemQuantityAllocatedDao @Default constructor(
    val count: Int? = 0
)

