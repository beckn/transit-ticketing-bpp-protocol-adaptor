package transit.ticketing.bpp.protocol.protocol.discovery.services

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
import transit.ticketing.bpp.protocol.protocol.status.services.BppClientStatusService
import transit.ticketing.bpp.protocol.protocol.discovery.mappers.ProtocolOnSearchFactory
import transit.ticketing.bpp.protocol.protocol.external.provider.BapServiceFactory
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnSearch
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseMessage


@Service
class BapOnSearchService @Autowired constructor(
  private val bapServiceFactory: BapServiceFactory,
  private  val protocolOnSearchFactory: ProtocolOnSearchFactory
  ) {
  private val log: Logger = LoggerFactory.getLogger(BppClientStatusService::class.java)


  fun onSearch(subscriberDto: SubscriberDto, context: ProtocolContext, protocolRequest: ProtocolOnSearch)
          : Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Initiating OnSearch : {}. Context: {}", subscriberDto)
//      val request = protocolOnSearchFactory.create(context = context)
      val clientService = bapServiceFactory.getBapClient(subscriberDto?.subscriber_url)
      val httpResponse = clientService.onSearch(protocolRequest).execute()
      log.info("OnSearch response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        httpResponse.isInternalServerError() -> Left(BppError.Internal)
        !httpResponse.hasBody() -> Left(BppError.NullResponse)
        else -> {
          log.info("Successfully invoked OnSearch . Response: {}", httpResponse.body())
          Either.Right(ProtocolAckResponse(context, ResponseMessage.ack()))
        }
      }
    }.mapLeft {
      log.error("Error when initiating OnSearch", it)
      BppError.Internal
    }
  }

}