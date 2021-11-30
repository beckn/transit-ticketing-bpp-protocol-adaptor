package transit.ticketing.bpp.protocol.protocol.discovery.services

import arrow.core.Either
import arrow.core.Either.Left
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import retrofit2.mock.MockRetrofit
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.external.hasBody
import transit.ticketing.bpp.protocol.protocol.external.isAckNegative
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.provider.BppClientFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.status.services.BppClientStatusService
import transit.ticketing.bpp.protocol.protocol.shared.schemas.*


@Service
class BppClientSearchService @Autowired constructor(
  private val bppServiceClientFactory: BppClientFactory,
  @Value("\${client_service.url}") val clientUrl: String,

  ) {
  private val log: Logger = LoggerFactory.getLogger(BppClientStatusService::class.java)

  fun search(subscriberDto: SubscriberDto, context: ProtocolContext, intent: ProtocolIntent)
      : Either<BppError, ProtocolOnSearch> {
    return Either.catch {
      log.info("Initiating Search using gateway: {}. Context: {}", subscriberDto, context)
      val clientService = bppServiceClientFactory.getClient(clientUrl)
      val httpResponse = clientService.search(buildProtocolSearchRequest(context, intent)).execute()
      log.info("Search response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        httpResponse.isInternalServerError() -> Left(BppError.Internal)
        !httpResponse.hasBody() -> Left(BppError.NullResponse)
        else -> {
          log.info("Successfully invoked search on gateway. Response: {}", httpResponse.body())
          Either.Right(httpResponse.body()!!)
        }
      }
    }.mapLeft {
      log.error("Error when initiating search", it)
      BppError.Internal
    }
  }
  fun onSearch(subscriberDto: SubscriberDto, request: ProtocolOnSearch)
          : Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Initiating Search using gateway: {}. Context: {}", subscriberDto, request)
      val clientService = bppServiceClientFactory.getClient(clientUrl)
      val httpResponse = clientService.onSearch(request).execute()
      log.info("Search response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        httpResponse.isInternalServerError() -> Left(BppError.Internal)
        !httpResponse.hasBody() -> Left(BppError.NullResponse)
        else -> {
          log.info("Successfully invoked search on gateway. Response: {}", httpResponse.body())
          Either.Right(httpResponse.body()!!)
        }
      }
    }.mapLeft {
      log.error("Error when initiating search", it)
      BppError.Internal
    }
  }
  private fun buildProtocolSearchRequest(context: ProtocolContext, intent: ProtocolIntent) =
    ProtocolSearchRequest(
      context,
      ProtocolSearchRequestMessage(
        intent
      )
    )

}