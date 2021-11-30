package transit.ticketing.bpp.protocol.schemas.factories

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import transit.ticketing.bpp.protocol.protocol.ProtocolVersion
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ProtocolContext
import java.time.Clock

@Component
class ContextFactory @Autowired constructor(
  @Value("\${context.domain}") private val domain: String,
  @Value("\${context.city}") private val city: String,
  @Value("\${context.country}") private val country: String,
  @Value("\${context.bpp_id}") private val bppId: String,
  @Value("\${context.bpp_uri}") private val bppUrl: String,
  private val uuidFactory: UuidFactory,
  private val clock: Clock = Clock.systemUTC()
) {
  fun create(
    transactionId: String = uuidFactory.create(),
    messageId: String = uuidFactory.create(),
    action: ProtocolContext.Action? = ProtocolContext.Action.SEARCH,
    bapId: String? = null,
    ) = ProtocolContext(
    domain = domain,
    country = country,
    city = city,
    action = action,
    coreVersion = ProtocolVersion.V0_9_1.value,
    bppId = bppId,
    bppUri = bppUrl,
    bapId = bapId,
    transactionId = transactionId,
    messageId = messageId,
    clock = clock,
  )
}