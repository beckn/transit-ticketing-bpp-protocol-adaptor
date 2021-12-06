package transit.ticketing.bpp.protocol.protocol.confirm.services

import arrow.core.Either
import arrow.core.Either.Left
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.confirm.mappers.ProtocolOnConfirmFactory
import transit.ticketing.bpp.protocol.protocol.external.hasBody
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.provider.BppClientFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.ClientConfirmRequest
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.protocol.status.services.BppClientStatusService
import java.time.format.DateTimeFormatter


@Service
class BppClientConfirmService @Autowired constructor(
  private val bppServiceClientFactory: BppClientFactory,
  @Value("\${client_service.url}") val clientUrl: String,
  val protocolOnConfirmFactory: ProtocolOnConfirmFactory
  ) {
  private val log: Logger = LoggerFactory.getLogger(BppClientStatusService::class.java)

  fun blockTicket(
    subscriberDto: SubscriberDto, context: ProtocolContext,
    message: ProtocolConfirmRequestMessage
  ): Either<BppError, ProtocolOnConfirm> {
    return Either.catch {
      log.info("Initiating BookTicket using client Bpp: {}. Context: {}", subscriberDto, context)
      val clientService = bppServiceClientFactory.getClient(clientUrl)
      val request : ClientConfirmRequest= buildConfirmRequest(message,context)
      return if(request.seats != 0 ){
          val httpResponse = clientService.blockTicket(request).execute()
          log.info("Book Ticket response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
        when {
          httpResponse.isInternalServerError() -> Left(BppError.Internal)
          !httpResponse.hasBody() -> Left(BppError.NullResponse)
          else -> {
            log.info("Successfully invoked search on book ticket. Response: {}", httpResponse.body())
            Either.Right(protocolOnConfirmFactory.create(httpResponse.body()!!,context,message))
          }
          }
      } else{
          log.info("No Seat Available in request to block ticket. Response: {}")
        Left(BppError.NullResponse)
      }
    }.mapLeft {
      log.error("Error when book ticket fails", it)
      BppError.Internal
    }
  }

  private fun buildConfirmRequest(
    message: ProtocolConfirmRequestMessage,
    context: ProtocolContext
  ): ClientConfirmRequest {
    return ClientConfirmRequest(
      source = message.order.fulfillment.start?.location?.id ?: "",
      destination = message.order.fulfillment.end?.location?.id ?: "",
      tripId = message.order.fulfillment.id?:"",
      date = context.timestamp.toString(),
      seats = message.order?.items?.first()?.quantity?.count?:0,
    )
  }

}