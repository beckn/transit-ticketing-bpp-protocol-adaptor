package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

data class CardPayment (
	val paymentUrl : String ?= null,
	val referenceNo : String ?= null
)