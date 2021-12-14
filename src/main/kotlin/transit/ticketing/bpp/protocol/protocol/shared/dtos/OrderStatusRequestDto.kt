package transit.ticketing.bpp.protocol.protocol.shared.dtos

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOrder
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolRequest


data class OrderStatusRequestDto @Default constructor(
    override val context: ProtocolContext
) : ProtocolRequest

data class OrderStatusRequestMessageDto @Default constructor(
    val order: ProtocolOrder
)