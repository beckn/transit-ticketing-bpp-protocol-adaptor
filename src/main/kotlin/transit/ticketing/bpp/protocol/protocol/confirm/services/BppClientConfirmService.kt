package transit.ticketing.bpp.protocol.protocol.confirm.services

import arrow.core.Either
import arrow.core.Either.Left
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.message.entities.OnConfirmDao
import transit.ticketing.bpp.protocol.protocol.confirm.mappers.ProtocolOnConfirmFactory
import transit.ticketing.bpp.protocol.protocol.external.hasBody
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.provider.BppClientFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.dtos.ConfirmRequestMessageDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.*
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnConfirm
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnConfirmMessage
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolPayment
import transit.ticketing.bpp.protocol.protocol.status.services.BppClientStatusService


@Service
class BppClientConfirmService @Autowired constructor(
    private val bppServiceClientFactory: BppClientFactory,
    @Value("\${client_service.url}") val clientUrl: String,
    val protocolOnConfirmFactory: ProtocolOnConfirmFactory
) {
    private val log: Logger = LoggerFactory.getLogger(BppClientStatusService::class.java)

    fun blockTicket(
        subscriberDto: SubscriberDto?, context: ProtocolContext,
        message: ConfirmRequestMessageDto
    ): Either<BppError, OnConfirmDao> {
        return Either.catch {
            log.info("Initiating BlockTicket using client Bpp: {}. Context: {}", context)
            val clientService = bppServiceClientFactory.getClient(clientUrl)
            val request: ClientBlockTicketRequest = buildBlockRequest(message, context)
            return if (request.seats != 0) {
                val httpResponse = clientService.blockTicket(request).execute()
                log.info("Block Ticket response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
                when {
                    httpResponse.isInternalServerError() -> Left(BppError.Internal)
                    !httpResponse.hasBody() -> Left(BppError.NullResponse)
                    else -> {
                        log.info("Successfully invoked search on block ticket. Response: {}", httpResponse.body())
                        Either.Right(protocolOnConfirmFactory.create(httpResponse.body()!!, context))
                    }
                }
            } else {
                log.info("No Seat Available in request to block ticket. Response: {}")
                Left(BppError.BadRequestError)
            }
        }.mapLeft {
            log.error("Error when block ticket fails", it)
            BppError.Internal
        }
    }

    private fun buildBlockRequest(
        message: ConfirmRequestMessageDto,
        context: ProtocolContext
    ): ClientBlockTicketRequest {
        return ClientBlockTicketRequest(
            source = message.order?.fulfillment?.start?.location?.id ?: "",
            destination = message.order?.fulfillment?.end?.location?.id ?: "",
            tripId = message.order?.fulfillment?.id?.split("-")?.first() ?: "",
            date = Util.formatYYYYmmDD(context.timestamp.toString()) ?: "",
            seats = message.order?.items?.first()?.quantity?.count ?: 0,
            slot = Util.formatHHmm(message.order?.fulfillment?.start?.time?.timestamp.toString()) ?: "",
        )
    }

}