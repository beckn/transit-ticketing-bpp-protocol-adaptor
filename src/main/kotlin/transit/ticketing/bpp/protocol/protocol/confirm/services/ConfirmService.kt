package transit.ticketing.bpp.protocol.protocol.confirm.services

import arrow.core.Either
import arrow.core.flatMap
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.errors.database.DatabaseError
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
    val confirmRepository : ResponseStorageService<ProtocolOnConfirm, OnConfirmDao>,
    val statusRepository : ResponseStorageService<ProtocolOnOrderStatus, OnOrderStatusDao>,
) {
    val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

    fun postConfirmRequest(
        context: ProtocolContext,
        message: ConfirmRequestMessageDto
    ): Either<HttpError, ProtocolOnConfirm> {
        log.info("Confirm Service : Got init request with message: {} ", message)
        var subscriber: SubscriberDto? = null
        var arrivalDate: String? = null
        var departureDate: String? = null
        var tripId: String? = null
        if (message?.order == null || message.order.items.isNullOrEmpty()) {
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
            .flatMap { subscriberInfo ->
                if (!subscriberInfo.isNullOrEmpty()) {
                    subscriber = subscriberInfo.first()
                }
                if (context.transactionId != null) {
                    confirmRepository.findById(context.transactionId).fold(
                        {
                            // No info available for this Transaction Id in DB
                            bppClientConfirmService.blockTicket(null, context, message).fold(
                                {
                                    // Failed to block ticket in Bpp
                                    log.error("Confirm Service : Failed to block ticket in Bpp")
                                    return Either.Left(it)
                                }, {
                                    // Received Response from Block ticket with message
                                    log.info("Confirm Service : Received Response from Block ticket with message")

                                    return updateOrder(it)
                                }
                            )
                        },
                        {
                            log.info("Confirm Service : Received Response from Db with message")
                            return Either.Right(it!!)
                        }
                    )
                } else {
                    log.error("Confirm Service : No Transaction Id available")
                    return Either.Left(BppError.BadRequestError)
                }
            }
    }

    fun updateOrder(onConfirmDao: OnConfirmDao): Either<DatabaseError, ProtocolOnConfirm> {
        return if (onConfirmDao.context?.transactionId == null) {
            log.error("Confirm Service :Transaction id is not available")
            Either.Left(DatabaseError.NotFound)
        } else {
            log.info("Confirm Service : Updating db on confirm callback")
            return confirmRepository.updateDocByQuery(
                OnConfirmDao::context / ProtocolContext::transactionId eq onConfirmDao.context.transactionId,
                onConfirmDao
            )
        }
    }

}
