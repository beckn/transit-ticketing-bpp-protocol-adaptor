package transit.ticketing.bpp.protocol.protocol.common.factories

import transit.ticketing.bpp.protocol.protocol.common.City
import transit.ticketing.bpp.protocol.protocol.common.Country
import transit.ticketing.bpp.protocol.protocol.common.Domain

import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory
import transit.ticketing.bpp.protocol.schemas.factories.UuidFactory
import java.time.Clock

class ContextFactoryInstance {
  companion object {
    fun create(uuidFactory: UuidFactory = UuidFactory(), clock: Clock = Clock.systemUTC()) = ContextFactory(
      domain = Domain.LocalRetail.value,
      city = City.Bengaluru.value,
      country = Country.India.value,
      bppId = "swtd_in_a_box_bap",
      bppUrl = "swtd_in_a_box_bap.com",
      uuidFactory = uuidFactory,
      clock = clock
    )
  }
}