package transit.ticketing.bpp.protocol.protocol.shared.schemas

data class ProtocolOrder @Default constructor(
    val provider: ProtocolSelectMessageSelectedProvider? = null,
    val items: List<ProtocolItem>,
    val billing: ProtocolBilling,
    val fulfillment: ProtocolFulfillment,
    val quote: ProtocolQuotation? = null,
    val payment: ProtocolPayment? = null, //todo: is this surely nullable?
    val id: String? = null,
    val state: String? = null,
    val createdAt: java.time.OffsetDateTime? = null,
    val updatedAt: java.time.OffsetDateTime? = null
)

data class ProtocolSelectMessageSelectedProvider @Default constructor(
    val id: String,
)

data class ProtocolSelectMessageSelectedOffers @Default constructor(
    val id: String
)