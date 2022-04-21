package transit.ticketing.bpp.protocol.message.entities

import com.fasterxml.jackson.annotation.JsonProperty
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default

data class ProtocolQuotationDao @Default constructor(
    val price: ProtocolPriceDao? = null,
    val breakup: List<ProtocolQuotationBreakupDao>? = null,
    val ttl: String? = null
)

data class ProtocolQuotationBreakupDao @Default constructor(
    val title: String? = null,
    val price: ProtocolPriceDao? = null
) {

    enum class TypeDao(val value: String) {
        ITEM("item"),
        OFFER("offer"),
        @JsonProperty("add-on")
        ADDON("add-on"),
        FULFILLMENT("fulfillment");
    }
}