package transit.ticketing.bpp.protocol.protocol.confirm.services

import arrow.core.Either
import arrow.core.flatMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.errors.database.DatabaseError
import transit.ticketing.bpp.protocol.errors.registry.RegistryLookupError
import transit.ticketing.bpp.protocol.message.entities.OnConfirmDao
import transit.ticketing.bpp.protocol.message.entities.OnOrderStatusDao
import transit.ticketing.bpp.protocol.message.mappers.GenericResponseMapper
import transit.ticketing.bpp.protocol.message.services.ResponseStorageService
import transit.ticketing.bpp.protocol.protocol.discovery.services.SearchService
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.dtos.ConfirmRequestMessageDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

@Service
class ConfirmService @Autowired constructor(
    private val registryService: RegistryService,
    private val bppClientConfirmService: BppClientConfirmService,
    private val bapOnConfirmService: BapOnConfirmService,
    private val mapper: GenericResponseMapper<ProtocolOnConfirm, OnConfirmDao>,
    val confirmRepository : ResponseStorageService<ProtocolOnConfirm, OnConfirmDao>,
) {
    val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

    fun confirm(
        context: ProtocolContext,
        message: ConfirmRequestMessageDto
    ): Either<HttpError, ProtocolAckResponse> {
        log.info("Confirm Service : Got init request with message: {} ", message)
        var subscriber: SubscriberDto? = null
        var arrivalDate: String? = null
        var departureDate: String? = null
        var tripId: String? = null
        if (message?.order == null || message.order.items.isNullOrEmpty()
            || message.order.fulfillment?.id == null) {
            log.info("Empty order received, no op. Order: {}", message)
            return Either.Left(BppError.BadRequestError)
        }
        if (message.order.fulfillment?.id != null) {
            val arrayOfFulfillment = message.order.fulfillment.id!!.split("-")
            if (arrayOfFulfillment.isNotEmpty() && arrayOfFulfillment.size == 5) {
                arrivalDate = Util.miliSecondsToDateString(arrayOfFulfillment[2])
                departureDate = Util.miliSecondsToDateString(arrayOfFulfillment[1])
                tripId = arrayOfFulfillment[0]
                var startLocation = ProtocolFulfillmentStart(
                    location = ProtocolLocation(id = arrayOfFulfillment[3]),
                    time = ProtocolTime(timestamp = departureDate!!)
                )
                var endLocation = ProtocolFulfillmentEnd(
                    location = ProtocolLocation(id = arrayOfFulfillment[4]),
                    time = ProtocolTime(timestamp = arrivalDate!!)
                )
                message.order.fulfillment.start = startLocation
                message.order.fulfillment.end = endLocation
            }
            if (tripId == null || arrivalDate.isNullOrEmpty() || departureDate.isNullOrEmpty()) {
                return Either.Left(BppError.BadRequestError)
            }
        }
        return registryService
            .lookupBapById(context.bapId!!)
            .flatMap<HttpError, List<SubscriberDto>, OnConfirmDao> { subscriberInfo ->
                subscriber = subscriberInfo.first()
                if ( subscriber != null && context.transactionId != null) {
                    // No info available for this Transaction Id in DB
                    bppClientConfirmService.blockTicket(subscriber!!, context, message)
                } else {
                    return Either.Left(BppError.BadRequestError)
                }

            }.flatMap {
                CoroutineScope(Dispatchers.IO).launch{ bapPostConfirm(subscriber!!, context, it )}
                return Either.Right(ProtocolAckResponse(context,ResponseMessage.ack()))
            }
    }

    suspend fun bapPostConfirm(subscriberDto: SubscriberDto, context: ProtocolContext,
                       onConfirmDao: OnConfirmDao ) {
            bapOnConfirmService.postOnConfirm(subscriberDto, context, mapper.entityToProtocol(onConfirmDao)).fold(
                {
                    // Failed to block ticket in Bpp
                    log.error("Confirm Service : Failed to block ticket in Bpp")
                }, {
                    // Received Response from Block ticket with message
                    log.info("Confirm Service : Received Response from Block ticket with message")
                    updateOrder(onConfirmDao)
                })
    }

    private fun updateOrder(onConfirmDao: OnConfirmDao) {
        log.error("Update Order Thread is ${Thread.currentThread().name}")
        if (onConfirmDao.context?.transactionId == null) {
            log.error("Confirm Service :Transaction id is not available")
        } else {
            val result =  confirmRepository.updateDocByQuery(
                OnConfirmDao::context / ProtocolContext::transactionId eq onConfirmDao.context.transactionId,
                onConfirmDao
            )
            log.info("Confirm Service : Updating db on confirm callback $result" )

        }
    }

}
