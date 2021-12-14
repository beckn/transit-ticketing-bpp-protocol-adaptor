package transit.ticketing.bpp.protocol.protocol.shared.dtos

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOrder
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolRequest


data class ConfirmRequestDto @Default constructor(
    override val context: ProtocolContext,
    val message: ConfirmRequestMessageDto
) : ProtocolRequest

data class ConfirmRequestMessageDto @Default constructor(
    val order: ProtocolOrder
)