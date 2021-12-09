package transit.ticketing.bpp.protocol.message.entities

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default

data class ProtocolFulfillmentDao @Default constructor(
    val id: String? = null,
    val start: ProtocolFulfillmentStartDao? = null,
    val end: ProtocolFulfillmentEndDao? = null,
)


// TODO Similar classes
data class ProtocolFulfillmentStartDao @Default constructor(
    val location: ProtocolLocationDao? = null,
    val time: ProtocolTimeDao? = null,
    val instructions: ProtocolDescriptorDao? = null,
    val contact: ProtocolContactDao? = null
)

// TODO Similar classes
data class ProtocolFulfillmentEndDao @Default constructor(
    val location: ProtocolLocationDao? = null,
    val time: ProtocolTimeDao? = null,
    val instructions: ProtocolDescriptorDao? = null,
    val contact: ProtocolContactDao? = null
)


data class ProtocolContactDao @Default constructor(
    val phone: String? = null,
    val email: String? = null,
    val tags: Map<String, String>? = null
)