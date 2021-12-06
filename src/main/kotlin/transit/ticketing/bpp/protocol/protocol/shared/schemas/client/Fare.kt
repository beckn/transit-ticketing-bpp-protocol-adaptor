package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

data class Fare (

	val amount : Int,
	val currency : String,
	val base : Float,
	val cgst : Float,
	val sgst : Float,
	val payment_type : String?
)