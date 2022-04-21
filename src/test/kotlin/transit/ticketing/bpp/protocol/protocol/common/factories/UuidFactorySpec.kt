package transit.ticketing.bpp.protocol.protocol.common.factories

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeTypeOf
import transit.ticketing.bpp.protocol.schemas.factories.UuidFactory
import java.util.*

internal class UuidFactorySpec : DescribeSpec() {
  init {
    it("should generate a new UUID") {
      val uuid = UuidFactory().create()

      uuid shouldNotBe null
      shouldNotThrowAny {
        UUID.fromString(uuid).shouldBeTypeOf<UUID>()
      }
    }
  }
}