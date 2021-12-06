package transit.ticketing.bpp.protocol.protocol.confirm.services

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
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolConfirmRequestMessage
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

@Service
class ConfirmService @Autowired constructor(
  private val registryService: RegistryService,
  private val bppClientConfirmService: BppClientConfirmService,
  private val bapOnConfirmService: BapOnConfirmService

) {
  val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

  fun postConfirmRequest(context: ProtocolContext, message: ProtocolConfirmRequestMessage): Either<HttpError, ProtocolAckResponse> {
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
            subscriber = it.first()
            bppClientConfirmService.blockTicket(subscriber, context, message)
        }.flatMap {
              bapOnConfirmService.onPostConfirm(subscriber, context, it)
        }
  }
}
