package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

import com.fasterxml.jackson.annotation.JsonProperty

data class Availability (
	val seats : Int,
	val trip_id : Int,
	@JsonProperty("arrival")
	val arrival : StopInfo,
	@JsonProperty("departure")
	val departure : StopInfo,
	val fare: Fare
)