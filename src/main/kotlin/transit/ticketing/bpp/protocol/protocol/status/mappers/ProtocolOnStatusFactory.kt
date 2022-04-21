package transit.ticketing.bpp.protocol.protocol.status.mappers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import transit.ticketing.bpp.protocol.message.entities.*
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.dtos.ConfirmRequestMessageDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.BlockBookResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.Trip
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import java.net.URI

@Component
class ProtocolOnStatusFactory @Autowired constructor() {
  val arrival = "Arrival"
  val departure = "Departure"
  val providerIdCode = "SWTD"

  fun create(response: BlockBookResponse, context: ProtocolContext) = OnOrderStatusDao(
    context = context,
    error = null,
    message = OnOrderStatusMessageDao(
      order = ProtocolOrderDao(
        id = response.ticket_no?.toString(),
        state = "Active",
        provider = ProtocolSelectMessageSelectedProviderDao(
          id = providerIdCode
        ),
        boatId = response.trip.boat_id,
        items = listOf(ProtocolItemDao(
          id = "ONE_WAY_TICKET" ,
          fulfillmentId = generateFullFillmentId(trip = response.trip),
          descriptor = ProtocolDescriptorDao(
            code = response.ticket_code?:null
          ),
          price = ProtocolPriceDao(
            currency=response.fare.currency,
            value = response.fare.amount.toString()
          ),
          quantity = ProtocolItemQuantityAllocatedDao(
            count = response.trip.seats
          )
        )),
        billing = ProtocolBillingDao(name = "TestUser"),
        fulfillment = ProtocolFulfillmentDao(
          id = generateFullFillmentId(trip = response.trip),
          start = ProtocolFulfillmentStartDao(
            location =  ProtocolLocationDao(id = response.trip.source),
            time = ProtocolTimeDao(label = departure,
              timestamp = response.trip.departure?.timestamp?: "")
          ),
          end = ProtocolFulfillmentEndDao(
            location =  ProtocolLocationDao(id = response.trip.destination),
            time = ProtocolTimeDao(label = arrival, response.trip.arrival?.timestamp?: "")
          )
        ),
        quote = ProtocolQuotationDao(
          price = ProtocolPriceDao(
            currency = response.fare.currency,
            value = response.fare.amount.toString()
          ),
          breakup = listOf(
            ProtocolQuotationBreakupDao(
              title = "base",
              price = ProtocolPriceDao(currency = "INR",value = response.fare.base.toString() )
            ),
            ProtocolQuotationBreakupDao(
              title = "cgst",
              price = ProtocolPriceDao(currency = "INR",value = response.fare.cgst.toString() )
            ),
            ProtocolQuotationBreakupDao(
              title = "sgst",
              price = ProtocolPriceDao(currency = "INR",value = response.fare.sgst.toString() )
            )
          ),
          ttl = null
        ),
        payment = ProtocolPaymentDao(
          uri = URI("/protocol/v1/payment"),
          tlMethod = ProtocolPaymentDao.TlMethod.GET,
          type = ProtocolPaymentDao.Type.PREFULFILLMENT,
          status = ProtocolPaymentDao.Status.PAID
        )
      )
    )
  )

  private fun generateFullFillmentId(trip: Trip): String {
    val arrivalTiming = Util.dateToMiliseconds(trip.arrival?.timestamp?:"")
    val departTiming = Util.dateToMiliseconds(trip.departure?.timestamp?:"")
    val data = "${trip.trip_id}-$departTiming-$arrivalTiming-${trip.departure?.stopId}-${trip.arrival?.stopId}"
    return data
  }
}