package transit.ticketing.bpp.protocol.protocol.shared.schemas.client

data class BlockBookResponse (
    val ticket_no : Int,
    val trip : Trip,
    val fare : Fare,
    val upi_payment : UpiPayment,
    val card_payment : CardPayment,
    val ticket_code : String ?= null
)