package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

data class Fare (

	val amount : Float,
	val currency : String,
	val base : Float,
	val cgst : Float,
	val sgst : Float,
	val paymentType : String?= null
)