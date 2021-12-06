package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

data class ClientConfirmRequest(
    val source : String,
    val destination : String,
    val date : String,
    val seats : Int,
    val tripId : String
)