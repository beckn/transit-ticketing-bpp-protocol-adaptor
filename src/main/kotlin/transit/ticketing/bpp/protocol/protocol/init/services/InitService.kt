package transit.ticketing.bpp.protocol.protocol.init.services

import arrow.core.Either
import arrow.core.flatMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.discovery.services.SearchService
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolInitRequestMessage
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

@Service
class InitService @Autowired constructor(
  private val registryService: RegistryService,
  private val bppClientInitService: BppClientInitService,
  private val bapOnInitService: BapOnInitService
) {
  val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

  fun postInit(context: ProtocolContext, message: ProtocolInitRequestMessage): Either<HttpError, ProtocolAckResponse> {
    log.info("Got init request with message: {} ", message)
    if (message?.order == null ||
            message.order.items.isNullOrEmpty()) {
      log.info("Empty order received, no op. Order: {}", message)
      return Either.Left(BppError.BadRequestError)
    }
    lateinit var subscriber : SubscriberDto
    return registryService
        .lookupBapById(context.bapId!!)
        .flatMap {
            subscriber=   it.first()
            bppClientInitService.blockTicket(subscriber, context, message)
        }.flatMap {
            bapOnInitService.onPostInit(subscriber, context, it)
        }
  }
}
