package transit.ticketing.bpp.protocol.protocol.status.services

import arrow.core.Either
import arrow.core.Either.Left
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.external.hasBody
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.external.provider.BapServiceFactory
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*


@Service
class BapOnStatusService @Autowired constructor(
  private val bapServiceFactory: BapServiceFactory,
  ) {
  private val log: Logger = LoggerFactory.getLogger(BppClientStatusService::class.java)



  fun onStatus(
    subscriberDto: SubscriberDto, context: ProtocolContext,
    request: ProtocolOnConfirm
  ): Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Initiating Search using gateway: {}. Context: {}", subscriberDto, context)

      val clientService = bapServiceFactory.getBapClient(subscriberDto.subscriber_url)
      val httpResponse = clientService.onConfirm(request).execute()

      log.info("Search response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        httpResponse.isInternalServerError() -> Left(BppError.Internal)
        !httpResponse.hasBody() -> Left(BppError.NullResponse)
        else -> {
          log.info("Successfully invoked search on confirm. Response: {}", httpResponse.body())
          Either.Right(ProtocolAckResponse(context=context,message = ResponseMessage.ack()))
        }
      }
    }.mapLeft {
      log.error("Error when initiating search", it)
      BppError.Internal
    }
  }
}