package transit.ticketing.bpp.protocol.protocol.init.services

import arrow.core.Either
import arrow.core.Either.Left
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.status.services.BppClientStatusService
import transit.ticketing.bpp.protocol.protocol.external.hasBody
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.provider.BapServiceFactory
import transit.ticketing.bpp.protocol.protocol.external.provider.BppClientFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.init.mappers.ProtocolOnInitFactory
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*


@Service
class BapOnInitService @Autowired constructor(
  private val bapClientFactory: BapServiceFactory,
  @Value("\${client_service.url}") val clientUrl: String
  ) {
  private val log: Logger = LoggerFactory.getLogger(BppClientStatusService::class.java)

  fun onPostInit(
    subscriberDto: SubscriberDto, context: ProtocolContext,
    request: ProtocolOnInit
  ): Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Initiating Search using gateway: {}. Context: {}", subscriberDto, context)
      val clientService = bapClientFactory.getBapClient(clientUrl)
      val httpResponse = clientService.onInit(request).execute()

      log.info("Search response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        httpResponse.isInternalServerError() -> Left(BppError.Internal)
        !httpResponse.hasBody() -> Left(BppError.NullResponse)
        else -> {
          log.info("Successfully invoked search on gateway. Response: {}", httpResponse.body())
          Either.Right(ProtocolAckResponse(context=context,message = ResponseMessage.ack()))
        }
      }
    }.mapLeft {
      log.error("Error when initiating search", it)
      BppError.Internal
    }
  }
}