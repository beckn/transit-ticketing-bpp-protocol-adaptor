package transit.ticketing.bpp.protocol.protocol.common.factories

import transit.ticketing.bpp.protocol.protocol.shared.Util.getCurrentDateInString
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.*
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*

class ResponseFactory {
    companion object {
        fun getDefault(context: ProtocolContext) = ProtocolAckResponse(
            context = context, message = ResponseMessage(ProtocolAck(ResponseStatus.ACK))
        )

        fun getSearchResponseFromBpp() = SearchResponse(
            trip = Trip(source = "101", destination = "104", date = "2022-01-11T10:31:41.532Z"),
            availability = listOf(
                Availability(
                    seats = 1, trip_id = 11,
                    fare = Fare(amount = 100f, currency = "INR", base = 0f, cgst = 1f, sgst = 2f),
                    arrival = StopInfo(slot = "10:20", timestamp = getCurrentDateInString(), stopId = "101"),
                    departure = StopInfo(slot = "10:31", timestamp = getCurrentDateInString(), stopId = "104")
                )
            )
        )

        fun getConfirmResponseFromBpp() = BlockBookResponse(
            ticket_no = 1237890,
            trip = Trip(
                source = "101",
                destination = "104",
                arrival = StopInfo(slot = "10:20", timestamp = getCurrentDateInString(), stopId = "101"),
                departure = StopInfo(slot = "10:31", timestamp = getCurrentDateInString(), stopId = "104"),
                date = getCurrentDateInString()
            ),
            fare = Fare(amount = 100f, currency = "INR", base = 0f, cgst = 1f, sgst = 2f),
            upi_payment = UpiPayment(),
            card_payment = CardPayment(),

            )

        fun getBookResponseFromBpp() = BlockBookResponse(
            ticket_no = 1237890,
            trip = Trip(
                source = "101",
                destination = "104",
                arrival = StopInfo(slot = "10:20", timestamp = getCurrentDateInString(), stopId = "101"),
                departure = StopInfo(slot = "10:31", timestamp = getCurrentDateInString(), stopId = "104"),
                date = getCurrentDateInString()
            ),
            fare = Fare(amount = 100f, currency = "INR", base = 0f, cgst = 1f, sgst = 2f),
            upi_payment = UpiPayment(),
            card_payment = CardPayment(),
            ticket_code = "ticketcode"
            )

        fun getStatusFromBpp(context: ProtocolContext,currentDateTimeInMS :String) = ProtocolOnOrderStatus(
            context = context,
            message = ProtocolOnOrderStatusMessage(
                order = ProtocolOrder(
                    provider = ProtocolSelectMessageSelectedProvider(
                        id = "KSWTD"
                    ),
                    items= listOf(ProtocolItem(
                        id = "ONE_WAY_TICKET",
                        fulfillmentId = "2001-$currentDateTimeInMS-$currentDateTimeInMS-100-101",
                        descriptor = ProtocolDescriptor(name = "Kochi"),
                        price = ProtocolPrice(currency = "INR",value = "122"),
                        quantity = ProtocolItemQuantityAllocated(count = 1)
                    )),
                    fulfillment = ProtocolFulfillment(
                        id = "2001-$currentDateTimeInMS-$currentDateTimeInMS-100-101",
                        start = ProtocolFulfillmentStart(),
                        end = ProtocolFulfillmentEnd()
                    )
                )

            )
        )

    }
}