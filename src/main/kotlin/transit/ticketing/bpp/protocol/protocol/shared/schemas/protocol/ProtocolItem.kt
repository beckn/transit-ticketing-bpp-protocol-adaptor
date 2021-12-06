package transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol

data class ProtocolItem @Default constructor(
    val id: String? = null,
    val fulfillmentId: String? = null,
    val descriptor: ProtocolDescriptor? = null,
    val price: ProtocolPrice? = null,
    val quantity: ProtocolItemQuantityAllocated
)