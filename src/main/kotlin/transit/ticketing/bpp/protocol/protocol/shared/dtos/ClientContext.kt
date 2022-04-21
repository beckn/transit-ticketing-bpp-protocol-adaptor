package transit.ticketing.bpp.protocol.protocol.shared.dtos

import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default
import transit.ticketing.bpp.protocol.schemas.factories.UuidFactory

data class ClientContext @Default constructor(
  val transactionId: String = UuidFactory().create(),
  val bapId: String? = null,
)