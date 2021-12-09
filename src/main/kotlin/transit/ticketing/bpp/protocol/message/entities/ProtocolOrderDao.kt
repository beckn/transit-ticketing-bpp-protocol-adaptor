package transit.ticketing.bpp.protocol.message.entities

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default

data class ProtocolOrderDao @Default constructor(
    val provider: ProtocolSelectMessageSelectedProviderDao? = null,
    val items: List<ProtocolItemDao>,
    val billing: ProtocolBillingDao,
    val fulfillment: ProtocolFulfillmentDao,
    val quote: ProtocolQuotationDao? = null,
    val payment: ProtocolPaymentDao? = null, //todo: is this surely nullable?
    val id: String? = null,
    val state: String? = null,
    val createdAt: java.time.OffsetDateTime? = null,
    val updatedAt: java.time.OffsetDateTime? = null
)

data class ProtocolSelectMessageSelectedProviderDao @Default constructor(
    val id: String,
)

data class ProtocolSelectMessageSelectedOffers @Default constructor(
    val id: String
)