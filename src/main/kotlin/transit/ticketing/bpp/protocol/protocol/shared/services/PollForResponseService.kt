package transit.ticketing.bpp.protocol.protocol.shared.services

import org.beckn.protocol.schemas.ProtocolResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class PollForResponseService<Protocol: ProtocolResponse> constructor(
) {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

}