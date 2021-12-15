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
import transit.ticketing.bpp.protocol.message.entities.OnOrderStatusDao
import transit.ticketing.bpp.protocol.protocol.external.hasBody
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.provider.BppClientFactory
import transit.ticketing.bpp.protocol.protocol.external.provider.BppServiceClient
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.dtos.OrderStatusRequestDto
import transit.ticketing.bpp.protocol.protocol.shared.dtos.OrderStatusRequestMessageDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.*
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.protocol.status.mappers.ProtocolOnStatusFactory


@Service
class BppClientStatusService @Autowired constructor(
    private val bppServiceClientFactory: BppClientFactory,
    @Value("\${client_service.url}") val clientUrl: String,
    val protocolOnStatusFactory: ProtocolOnStatusFactory
    ) {
    private val log: Logger = LoggerFactory.getLogger(BppClientStatusService::class.java)

    fun postStatus(
        subscriberDto: SubscriberDto, context: ProtocolContext,
        message: OrderStatusRequestMessageDto
    ): Either<BppError, ProtocolOnOrderStatus> {
        return Either.catch {
            log.info("Initiating Search using gateway: {}. Context: {}", subscriberDto, context)
            val clientService = bppServiceClientFactory.getClient(clientUrl)
            val httpResponse =
                invokeBppOrderStatusApi(
                    bppServiceClient = clientService,
                    context = context
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
        context: ProtocolContext
    ): Response<ProtocolOnOrderStatus> {
        val statusRequest = OrderStatusRequestDto(
            context = context
        )
        return bppServiceClient.status(statusRequest).execute()
    }

    fun bookTicket(
        context: ProtocolContext,
        message: ProtocolOnConfirmMessage,
        tripId: String
    ): Either<BppError, OnOrderStatusDao> {
        return Either.catch {
            log.info("Initiating  BookTicket using client Bpp: {}")
            val clientService = bppServiceClientFactory.getClient(clientUrl)
            val request: ClientBookTicketRequest = buildBookRequest(context, message, tripId)
            return if (request.ticket_no.isNotEmpty()) {
                val httpResponse = clientService.bookTicket(request).execute()
                log.info("Book Ticket response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
                when {
                    httpResponse.isInternalServerError() -> Left(BppError.Internal)
                    !httpResponse.hasBody() -> Left(BppError.NullResponse)
                    else -> {
                        log.info("Successfully invoked search on book ticket. Response: {}", httpResponse.body())
                        message.order?.ticket_code = httpResponse.body()?.ticket_code
                        message.order?.payment?.status = ProtocolPayment.Status.PAID
                        Either.Right(protocolOnStatusFactory.create(httpResponse.body()!!,context))
                    }
                }
            } else {
                log.info("Invalid request to book ticket. Response: {}")
                Left(BppError.BadRequestError)
            }
        }.mapLeft {
            log.error("Error when book ticket fails", it)
            BppError.Internal
        }
    }


    private fun buildBookRequest(
        context: ProtocolContext,
        message: ProtocolOnConfirmMessage,
        tripId: String
    ): ClientBookTicketRequest {
        val order = message?.order
        return ClientBookTicketRequest(
            ticket_no = order?.id ?: "",
            payment_type = "CASH",
            trip = Trip(
                boat_id = order?.boatId,
                trip_id = tripId,
                source = order?.fulfillment?.start?.location?.id!!,
                destination = order?.fulfillment?.end?.location?.id!!,
                selected_slot = Util.formatHHmm(context?.timestamp.toString()) ?: "",
                seats = order?.items?.get(0)?.quantity?.count,
                date = Util.formatYYYYmmDD(context?.timestamp.toString()) ?: "",
                arrival = StopInfo(
                    slot = Util.formatHHmm(order?.fulfillment?.end?.time?.timestamp.toString())?:"" ,
                    stopId = order?.fulfillment?.end?.location?.id!!,
                    timestamp= order?.fulfillment?.end?.time?.timestamp?:""
                ),
                departure = StopInfo(
                    slot = Util.formatHHmm(order?.fulfillment?.start?.time?.timestamp.toString())?:"",
                    stopId = order?.fulfillment?.start?.location?.id!!,
                    timestamp= order?.fulfillment?.start?.time?.timestamp?:""
                )
            ),
            fare = Fare(
                amount = order.quote?.price?.value?.toFloat()!!,
                currency = order.quote?.price?.currency,
                base = order.quote?.breakup?.get(0)?.price?.value?.toFloat()!!,
                cgst = order.quote?.breakup?.get(1)?.price?.value?.toFloat()!!,
                sgst = order.quote?.breakup?.get(2)?.price?.value?.toFloat()!!,
            ),
            upiPayment = UpiPayment(
                paymentUrl = message.order.payment?.uri.toString(),
                referenceNo = ""
            ),
            cardPayment = CardPayment(
                paymentUrl = message.order.payment?.uri.toString(),
                referenceNo = ""
            )
        )
    }

}