package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

data class Trip(
    val source: String,
    val destination: String,
    val date: String,
    val selected_slot: String? = null,
    val seats: Int? = null,
    val trip_id: String? = null,
    val boat_id: String? = null,
    val arrival: StopInfo? = null,
    val departure: StopInfo? = null,
)