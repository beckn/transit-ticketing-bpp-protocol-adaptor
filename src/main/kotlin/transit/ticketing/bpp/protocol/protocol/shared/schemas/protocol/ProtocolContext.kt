package transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import transit.ticketing.bpp.protocol.schemas.factories.UuidFactory
import java.time.Clock
import java.time.OffsetDateTime

data class ProtocolContext @Default constructor(
    val domain: String,
    val country: String,
    val city: String,
    val action: Action?,
    val coreVersion: String,
    val bapId: String? = null,
    val bapUri: String? = null,
    val bppId: String? = null,
    val bppUri: String? = null,
    val transactionId: String =  UuidFactory().create(),
    val messageId: String =  UuidFactory().create(),
    @JsonIgnore val clock: Clock = Clock.systemUTC(),
    val timestamp: OffsetDateTime = OffsetDateTime.now(clock),
    val key: String? = null,
    val ttl: String? = null,
) {
    enum class Action(val value: String) {
        @JsonProperty("search")
        SEARCH("search"),
        @JsonProperty("init")
        INIT("`init`"),
        @JsonProperty("confirm")
        CONFIRM("confirm"),
        @JsonProperty("status")
        STATUS("status"),
        @JsonProperty("cancel")
        CANCEL("cancel"),
        @JsonProperty("support")
        SUPPORT("support"),
        @JsonProperty("on_search")
        ON_SEARCH("on_search"),
        @JsonProperty("on_init")
        ON_INIT("on_init"),
        @JsonProperty("on_confirm")
        ON_CONFIRM("on_confirm"),
        @JsonProperty("on_status")
        ON_STATUS("on_status"),
        @JsonProperty("on_cancel")
        ON_CANCEL("on_cancel"),
        @JsonProperty("on_support")
        ON_SUPPORT("on_support")
    }
}