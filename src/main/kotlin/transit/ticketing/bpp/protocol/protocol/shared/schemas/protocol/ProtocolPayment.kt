package transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI

data class ProtocolPayment @Default constructor(
    val uri: URI? = null,
    val tlMethod: TlMethod? = null,
    val params: Map<String, String>? = null,
    val type: Type?= null,
    var status: Status? = null,
    val time: ProtocolTime? = null
) {
    /**
     * Values: get,post
     */
    enum class TlMethod(val value: String) {
        @JsonProperty("http/get")
        GET("http/get"),
        @JsonProperty("http/post")
        POST("http/post");
    }

    /**
     *
     * Values: oNMinusORDER,pREMinusFULFILLMENT,oNMinusFULFILLMENT,pOSTMinusFULFILLMENT
     */
    enum class Type(val value: String) {
        @JsonProperty("ON-ORDER")
        ONORDER("ON-ORDER"),
        @JsonProperty("PRE-FULFILLMENT")
        PREFULFILLMENT("PRE-FULFILLMENT"),
        @JsonProperty("ON-FULFILLMENT")
        ONFULFILLMENT("ON-FULFILLMENT"),
        @JsonProperty("POST-FULFILLMENT")
        POSTFULFILLMENT("POST-FULFILLMENT");
    }

    enum class Status(val value: String) {
        PAID("PAID"),
        @JsonProperty("NOT-PAID")
        NOTPAID("NOT-PAID");
    }
}