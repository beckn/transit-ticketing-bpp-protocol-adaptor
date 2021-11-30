package transit.ticketing.bpp.protocol.protocol.shared.schemas

data class ProtocolOnOrderStatus @Default constructor(
    override val context: ProtocolContext? = null,
    val message: ProtocolOnOrderStatusMessage?,
    override val error: ProtocolError? = null
) : ProtocolResponse

data class ProtocolOnOrderStatusMessage @Default constructor(
    val order: ProtocolOrder? = null
)

