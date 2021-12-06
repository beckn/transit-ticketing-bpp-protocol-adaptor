package transit.ticketing.bpp.protocol.message.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default
import java.time.Clock
import java.time.OffsetDateTime

data class ContextDao @Default constructor(
  val domain: Domain,
  val country: String,
  val city: String,
  val action: Action,
  val coreVersion: String,
  val bapId: String? = null,
  val bapUri: String? = null,
  val bppId: String? = null,
  val bppUri: String? = null,
  val transactionId: String,
  val messageId: String,
  @JsonIgnore val clock: Clock = Clock.systemUTC(),
  val timestamp: OffsetDateTime = OffsetDateTime.now(clock),
  val key: String? = null,
  val ttl: String? = null
) {
  enum class Action(val value: String) {
    @JsonProperty("search") SEARCH("search"),
    @JsonProperty("select") SELECT("select"),
    @JsonProperty("init") INIT("`init`"),
    @JsonProperty("confirm") CONFIRM("confirm"),
    @JsonProperty("update") UPDATE("update"),
    @JsonProperty("status") STATUS("status"),
    @JsonProperty("track") TRACK("track"),
    @JsonProperty("cancel") CANCEL("cancel"),
    @JsonProperty("rating") RATING("rating"),
    @JsonProperty("support") SUPPORT("support"),
    @JsonProperty("on_search") ON_SEARCH("on_search"),
    @JsonProperty("on_select") ON_SELECT("on_select"),
    @JsonProperty("on_init") ON_INIT("on_init"),
    @JsonProperty("on_confirm") ON_CONFIRM("on_confirm"),
    @JsonProperty("on_update") ON_UPDATE("on_update"),
    @JsonProperty("on_status") ON_STATUS("on_status"),
    @JsonProperty("on_track") ON_TRACK("on_track"),
    @JsonProperty("on_cancel") ON_CANCEL("on_cancel"),
    @JsonProperty("on_rating") ON_RATING("on_rating"),
    @JsonProperty("on_support") ON_SUPPORT("on_support")
  }
}

typealias Domain = String
typealias Duration = String