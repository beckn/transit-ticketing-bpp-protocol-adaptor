package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

import com.fasterxml.jackson.annotation.JsonProperty

data class ClientBookTicketRequest(
    val ticket_no : String,
    val payment_type : String,
    val trip : Trip,
    val fare : Fare,
    @JsonProperty("upi_payment") val upiPayment: UpiPayment ?= null,
    @JsonProperty("card_payment") val cardPayment: CardPayment ?= null,
)