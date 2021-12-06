package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

data class StopInfo (
    val slot : String,
    val timestamp : String,
    val label : String,
    val stopId : String,
)