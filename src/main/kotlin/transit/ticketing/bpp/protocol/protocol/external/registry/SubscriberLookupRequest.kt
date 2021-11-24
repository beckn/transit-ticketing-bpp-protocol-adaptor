package transit.ticketing.bpp.protocol.protocol.external.registry

import transit.ticketing.bpp.protocol.protocol.external.domains.Subscriber


data class SubscriberLookupRequest(
  val subscriber_id: String? = null,
  val type: Subscriber.Type,
  val domain: String,
  val country: String,
  val city: String
)