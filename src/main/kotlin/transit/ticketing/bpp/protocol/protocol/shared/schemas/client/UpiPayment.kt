package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

data class UpiPayment (

	val paymentUrl : String ?= null,
	val referenceNo : String ?= null
)