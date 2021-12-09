package transit.ticketing.bpp.protocol.message.entities

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext

data class OnOrderStatusDao @Default constructor(
    override val context: ProtocolContext?,
    override val error: ErrorDao?,
    val message: OnOrderStatusMessageDao?,
) : BecknResponseDao

data class OnOrderStatusMessageDao @Default constructor(
    val order: ProtocolOrderDao? = null
)

