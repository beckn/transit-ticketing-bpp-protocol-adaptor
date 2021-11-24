package transit.ticketing.bpp.protocol.protocol.external.registry

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Clock
import java.time.LocalDateTime

data class SubscriberDto(
  val subscriber_id: String,
  val subscriber_url: String,
  val type: Type,
  val domain: String,
  val city: String,
  val country: String,
  val signing_public_key: String,
  val encr_public_key: String,
  val status: Status,

  @JsonIgnore
  val clock: Clock = Clock.systemUTC(),
  val valid_from: LocalDateTime = LocalDateTime.now(clock),
  val valid_until: LocalDateTime = LocalDateTime.now(clock),
  val created: LocalDateTime = LocalDateTime.now(clock),
  val updated: LocalDateTime = LocalDateTime.now(clock)
) {

  enum class Type {
    BAP, BPP, BG, LREG, CREG, RREG
  }

  enum class Status {
    INITIATED, UNDER_SUBSCRIPTION, SUBSCRIBED, EXPIRED, UNSUBSCRIBED, INVALID_SSL
  }
}

