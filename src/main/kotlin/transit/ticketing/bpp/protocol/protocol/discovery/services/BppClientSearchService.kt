package transit.ticketing.bpp.protocol.protocol.discovery.services

import arrow.core.Either
import arrow.core.Either.Left
import org.beckn.protocol.schemas.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.external.hasBody
import transit.ticketing.bpp.protocol.protocol.external.isAckNegative
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.provider.BppClientFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.dtos.SearchCriteria


@Service
class BppClientSearchService @Autowired constructor(
  private val bppServiceClientFactory: BppClientFactory
) {
  private val log: Logger = LoggerFactory.getLogger(BppClientSearchService::class.java)

  fun search(gateway: SubscriberDto, context: ProtocolContext, criteria: SearchCriteria)
      : Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Initiating Search using gateway: {}. Context: {}", gateway, context)
      val gatewayServiceClient = bppServiceClientFactory.getClient(gateway.subscriber_url)
      val httpResponse = gatewayServiceClient.search(buildProtocolSearchRequest(context, criteria)).execute()
      log.info("Search response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        httpResponse.isInternalServerError() -> Left(BppError.Internal)
        !httpResponse.hasBody() -> Left(BppError.NullResponse)
        httpResponse.isAckNegative() -> Left(BppError.Nack)
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

  private fun buildProtocolSearchRequest(context: ProtocolContext, criteria: SearchCriteria) =
    ProtocolSearchRequest(
      context,
      ProtocolSearchRequestMessage(
        ProtocolIntent(
          item = ProtocolIntentItem(descriptor = ProtocolIntentItemDescriptor(name = criteria.searchString)),
          provider = ProtocolProvider(id = criteria.providerId,category_id = criteria.categoryId),
          fulfillment = ProtocolFulfillment(
            start = ProtocolFulfillmentStart(location = ProtocolLocation(gps=criteria.pickupLocation)),
            end = ProtocolFulfillmentEnd(location = ProtocolLocation(gps = criteria.deliveryLocation))),
        )
      )
    )
}