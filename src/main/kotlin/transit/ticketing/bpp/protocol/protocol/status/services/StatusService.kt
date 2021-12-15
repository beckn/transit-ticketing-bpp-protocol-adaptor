package transit.ticketing.bpp.protocol.protocol.status.services

import arrow.core.Either
import arrow.core.flatMap
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.message.entities.OnConfirmDao
import transit.ticketing.bpp.protocol.message.entities.OnOrderStatusDao
import transit.ticketing.bpp.protocol.message.services.ResponseStorageService
import transit.ticketing.bpp.protocol.protocol.discovery.services.SearchService
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

@Service
class StatusService @Autowired constructor(
    private val registryService: RegistryService,
    private val confirmRepository: ResponseStorageService<ProtocolOnConfirm, OnConfirmDao>,
    private val statusRepository: ResponseStorageService<ProtocolOnOrderStatus, OnOrderStatusDao>,
    private val bppClientStatusService: BppClientStatusService,

    ) {
    val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

    fun getOrderStatusRequest(
        context: ProtocolContext
    ): Either<HttpError, ProtocolOnOrderStatus> {
        log.info("Got status request with transactionId: {} ", context?.transactionId)
        return if (context?.transactionId == null) {
            log.info("Empty transaction received,no transaction: {}")
            Either.Left(BppError.BadRequestError)
        } else {
            registryService
                .lookupBapById(context.bapId!!)
                .flatMap {
                    statusRepository.findById(context.transactionId)
                }
        }
    }

    fun getPaymentDetails(transactionId: String): Either<HttpError, ProtocolOnOrderStatus> {
        return confirmRepository.findById(transactionId).flatMap {
            var arrivalDate: String? = null
            var departureDate: String? = null
            var tripId: String? = null
            val message = it.message

            if (message?.order == null || message?.order.items.isNullOrEmpty()) {
                log.info("Empty order On Confirm, no Order: {}")
                return Either.Left(BppError.Internal)
            }

            if (message?.order?.fulfillment?.id != null) {
                val arrayOfFulfillment = message?.order?.fulfillment.id!!.split("-")
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
                    message?.order.fulfillment.start = startLocation
                    message?.order.fulfillment.end = endLocation
                }
                if (tripId == null || arrivalDate.isNullOrEmpty() || departureDate.isNullOrEmpty()) {
                    return Either.Left(BppError.BadRequestError)
                }
                bppClientStatusService.bookTicket(it.context!!, message,tripId).flatMap { response ->
                    statusRepository.updateDocByQuery(
                        OnOrderStatusDao::context / ProtocolContext::transactionId eq it.context?.transactionId,
                        response
                    )
                }

            } else {
                log.info("Invalid FullfillmentId in onStatus")
                return Either.Left(BppError.Internal)
            }
        }
    }
}
