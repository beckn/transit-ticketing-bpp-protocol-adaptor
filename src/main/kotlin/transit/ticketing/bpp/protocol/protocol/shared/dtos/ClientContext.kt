package transit.ticketing.bpp.protocol.protocol.shared.dtos

import org.beckn.protocol.schemas.Default
import transit.ticketing.bpp.protocol.schemas.factories.UuidFactory

data class ClientContext @Default constructor(
  val transactionId: String = UuidFactory().create(),
  val bppId: String? = null,
)