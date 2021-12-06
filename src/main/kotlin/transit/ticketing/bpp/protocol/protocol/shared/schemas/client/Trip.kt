package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

data class Trip (
	val source : Int,
	val destination : Int,
	val date : String,
	val selected_slot : String ?= null,
	val seats : Int ?= null,
	val trip_id : Int ?= null,
	val boat_id : Int ?= null
)