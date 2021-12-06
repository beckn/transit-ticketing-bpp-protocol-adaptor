package transit.ticketing.bpp.protocol.protocol.shared.controllers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolResponse
import transit.ticketing.bpp.protocol.protocol.shared.services.PollForResponseService
import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory

open class AbstractPollForResponseController<Protocol: ProtocolResponse>(
  private val responseService: PollForResponseService<Protocol>,
  private val contextFactory: ContextFactory
) {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

}