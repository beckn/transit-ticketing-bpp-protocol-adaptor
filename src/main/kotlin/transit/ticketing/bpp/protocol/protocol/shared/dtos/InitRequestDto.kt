package transit.ticketing.bpp.protocol.protocol.shared.dtos

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOrder
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolRequest


data class InitRequestDto @Default constructor(
    override val context: ProtocolContext,
    val message: InitRequestMessageDto
) : ProtocolRequest

data class InitRequestMessageDto @Default constructor(
    val order: ProtocolOrder
)