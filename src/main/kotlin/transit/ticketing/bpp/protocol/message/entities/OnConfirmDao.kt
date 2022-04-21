package transit.ticketing.bpp.protocol.message.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOrder

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnConfirmDao @Default constructor(
    val message: ProtocolOnConfirmMessageDao,
    override val context: ProtocolContext?,
    override val error: ErrorDao?,
) : BecknResponseDao


data class ProtocolOnConfirmMessageDao @Default constructor(
    val order: ProtocolOrderDao? = null
)