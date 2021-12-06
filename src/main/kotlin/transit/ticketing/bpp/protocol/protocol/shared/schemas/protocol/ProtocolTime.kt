package transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol

data class ProtocolTime @Default constructor(
    val label: String? = null,
    val timestamp: String,
    val duration: String? = null,
    val range: ProtocolTimeRange? = null,
    val days: String? = null,
//    val schedule: ProtocolSchedule? = null
)

data class ProtocolTimeRange @Default constructor(
    val start: java.time.OffsetDateTime? = null,
    val end: java.time.OffsetDateTime? = null
)
