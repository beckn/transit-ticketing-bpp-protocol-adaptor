package transit.ticketing.bpp.protocol.protocol.init.mappers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.BlockBookResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import java.net.URI
import java.time.OffsetDateTime

@Component
class ProtocolOnInitFactory @Autowired constructor() {
  val arrival = "Arrival"
  val departure = "Departure"
  val providerIdCode = "SWTD"

  fun create(response : BlockBookResponse, context: ProtocolContext, message: ProtocolInitRequestMessage) = ProtocolOnInit(
    context = context,
    message = ProtocolOnInitMessage(
      order = ProtocolOnInitMessageInitialized(
        provider = ProtocolOnInitMessageInitializedProvider(
          id = providerIdCode
        ),
        items = message.order.items,
        billing = ProtocolBilling(name = message.order.billing.name),
        fulfillment = ProtocolFulfillment(
          id = message.order.fulfillment.id,
          start = ProtocolFulfillmentStart(
            location =  ProtocolLocation(id = response.trip.source.toString()),
            time = ProtocolTime(label = departure, timestamp = "")
          ),
          end = ProtocolFulfillmentEnd(
            location =  ProtocolLocation(id = response.trip.destination.toString()),
            time = ProtocolTime(label = arrival, timestamp = "")
          )
        ),
        quote = ProtocolQuotation(
          price = ProtocolPrice(
            currency =  response.fare.currency,
            value = response.fare.amount.toString()
          ),
          breakup = listOf(),
          ttl = null
        ),
        payment = ProtocolPayment(
          uri = URI(response.card_payment.payment_url),
          tlMethod = ProtocolPayment.TlMethod.GET,
          type = ProtocolPayment.Type.PREFULFILLMENT,
          status = ProtocolPayment.Status.NOTPAID
        )

      )
    )
  )
}