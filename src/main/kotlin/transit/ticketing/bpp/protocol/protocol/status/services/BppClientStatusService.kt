package transit.ticketing.bpp.protocol.protocol.status.services

import arrow.core.Either
import arrow.core.Either.Left
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import retrofit2.Response
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.external.hasBody
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.provider.BppClientFactory
import transit.ticketing.bpp.protocol.protocol.external.provider.BppServiceClient
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnOrderStatus
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOrderStatusRequest
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOrderStatusRequestMessage


@Service
class BppClientStatusService @Autowired constructor(
  private val bppServiceClientFactory: BppClientFactory,
  @Value("\${client_service.url}") val clientUrl: String,

  ) {
  private val log: Logger = LoggerFactory.getLogger(BppClientStatusService::class.java)

  fun postStatus(
    subscriberDto: SubscriberDto, context: ProtocolContext,
    message: ProtocolOrderStatusRequestMessage
  ): Either<BppError, ProtocolOnOrderStatus> {
    return Either.catch {
      log.info("Initiating Search using gateway: {}. Context: {}", subscriberDto, context)
      val clientService = bppServiceClientFactory.getClient(clientUrl)
      val httpResponse =
        invokeBppOrderStatusApi(
          bppServiceClient = clientService,
          context = context,
          message = message
        )
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

  private fun invokeBppOrderStatusApi(
    bppServiceClient: BppServiceClient,
    context : ProtocolContext,
    message: ProtocolOrderStatusRequestMessage
  ): Response<ProtocolOnOrderStatus> {
    val statusRequest = ProtocolOrderStatusRequest(
      context = context,
      message = message)
    return bppServiceClient.status(statusRequest).execute()
  }
}