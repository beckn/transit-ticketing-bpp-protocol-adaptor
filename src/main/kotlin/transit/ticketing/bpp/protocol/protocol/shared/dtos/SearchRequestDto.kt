package transit.ticketing.bpp.protocol.protocol.shared.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolIntent


@JsonInclude(JsonInclude.Include.NON_NULL)
data class SearchRequestDto @Default constructor(
  val context: ClientContext,
  val message: SearchRequestMessageDto,
)

data class SearchRequestMessageDto @Default constructor(
  val intent: ProtocolIntent
)

