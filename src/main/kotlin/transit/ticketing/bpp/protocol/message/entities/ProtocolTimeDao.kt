package transit.ticketing.bpp.protocol.message.entities

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default

data class ProtocolTimeDao @Default constructor(
    val label: String? = null,
    val timestamp: String,
    val duration: String? = null,
    val range: ProtocolTimeRangeDao? = null,
    val days: String? = null,
//    val schedule: ProtocolSchedule? = null
)

data class ProtocolTimeRangeDao @Default constructor(
    val start: java.time.OffsetDateTime? = null,
    val end: java.time.OffsetDateTime? = null
)
