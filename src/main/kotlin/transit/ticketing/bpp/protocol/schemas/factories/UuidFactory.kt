package transit.ticketing.bpp.protocol.schemas.factories

import org.springframework.stereotype.Component
import java.util.*

@Component
class UuidFactory {
  fun create() = UUID.randomUUID().toString()
}