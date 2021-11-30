package transit.ticketing.bpp.protocol.protocol.shared.schemas


data class ProtocolOnInit @Default constructor(
    override val context: ProtocolContext? = null,
    val message: ProtocolOnInitMessage?,
    override val error: ProtocolError? = null
) : ProtocolResponse

data class ProtocolOnInitMessage @Default constructor(
    val order: ProtocolOnInitMessageInitialized? = null
)

data class ProtocolOnInitMessageInitialized @Default constructor(
    val provider: ProtocolOnInitMessageInitializedProvider? = null,
    val items: List<ProtocolItem>? = null,
    val billing: ProtocolBilling? = null,
    val fulfillment: ProtocolFulfillment? = null,
    val quote: ProtocolQuotation? = null,
    val payment: ProtocolPayment? = null
)

data class ProtocolOnInitMessageInitializedProvider @Default constructor(
    val id: String? = null
)

