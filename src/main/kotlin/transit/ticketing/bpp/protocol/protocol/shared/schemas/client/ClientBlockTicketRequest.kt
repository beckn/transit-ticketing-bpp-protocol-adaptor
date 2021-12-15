package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

import com.fasterxml.jackson.annotation.JsonProperty

data class ClientBlockTicketRequest(
    val source : String,
    val destination : String,
    val date : String,
    val seats : Int,
    @JsonProperty("trip_id") val tripId : String,
)