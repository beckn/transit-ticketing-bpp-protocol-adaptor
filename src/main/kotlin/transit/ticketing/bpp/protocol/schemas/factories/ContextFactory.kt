package transit.ticketing.bpp.protocol.schemas.factories

import org.beckn.protocol.schemas.ProtocolContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import transit.ticketing.bpp.protocol.protocol.ProtocolVersion
import java.time.Clock

@Component
class ContextFactory @Autowired constructor(
  @Value("\${context.domain}") private val domain: String,
  @Value("\${context.city}") private val city: String,
  @Value("\${context.country}") private val country: String,
  @Value("\${context.bap_id}") private val bapId: String,
  @Value("\${context.bap_uri}") private val bapUrl: String,
  private val uuidFactory: UuidFactory,
  private val clock: Clock = Clock.systemUTC()
) {
  fun create(
    transactionId: String = uuidFactory.create(),
    messageId: String = uuidFactory.create(),
    action: ProtocolContext.Action? = ProtocolContext.Action.SEARCH
  ) = ProtocolContext(
    domain = domain,
    country = country,
    city = city,
    action = action,
    coreVersion = ProtocolVersion.V0_9_1.value,
    bapId = bapId,
    bapUri = bapUrl,
    transactionId = transactionId,
    messageId = messageId,
    clock = clock,
  )
}