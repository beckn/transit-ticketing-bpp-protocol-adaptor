package transit.ticketing.bpp.protocol.protocol.discovery.services

import arrow.core.Either
import arrow.core.Either.Left
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.external.hasBody
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.provider.BppClientFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.status.services.BppClientStatusService
import transit.ticketing.bpp.protocol.protocol.discovery.mappers.ProtocolOnSearchFactory
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.ClientSearchRequest
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*


@Service
class BppClientSearchService @Autowired constructor(
  private val bppServiceClientFactory: BppClientFactory,
  @Value("\${client_service.url}") val clientUrl: String,
  private  val protocolOnSearchFactory: ProtocolOnSearchFactory
  ) {
  private val log: Logger = LoggerFactory.getLogger(BppClientStatusService::class.java)

  fun search(subscriberDto: SubscriberDto?, context: ProtocolContext, intent: ProtocolIntent)
      : Either<BppError, ProtocolOnSearch> {
    return Either.catch {
      log.info("Initiating Search using gateway: {}. Context: {}", context)
      val clientService = bppServiceClientFactory.getClient(clientUrl)
      val request = ClientSearchRequest(origin = intent.fulfillment?.start?.location?.gps.toString(),
        destination = intent.fulfillment?.end?.location?.gps.toString())
      val httpResponse = clientService.search(request.origin,request.destination).execute()
      log.info("Search response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        httpResponse.isInternalServerError() -> Left(BppError.Internal)
        !httpResponse.hasBody() -> Left(BppError.NullResponse)
        else -> {
          log.info("Successfully invoked search on gateway. Response: {}", httpResponse.body())
          Either.Right(protocolOnSearchFactory.create(httpResponse.body()!!,context,intent))
        }
      }
    }.mapLeft {
      log.error("Error when initiating search", it)
      BppError.Internal
    }
  }

}