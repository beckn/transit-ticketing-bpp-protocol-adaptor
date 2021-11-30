package transit.ticketing.bpp.protocol.protocol.shared.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ProtocolResponse

open class PollForResponseService<Protocol: ProtocolResponse> constructor(
) {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

}