package transit.ticketing.bpp.protocol.protocol.common.factories

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import transit.ticketing.bpp.protocol.protocol.common.City
import transit.ticketing.bpp.protocol.protocol.common.Country
import transit.ticketing.bpp.protocol.protocol.common.Domain
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import transit.ticketing.bpp.protocol.protocol.ProtocolVersion
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory
import transit.ticketing.bpp.protocol.schemas.factories.UuidFactory
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class ContextFactorySpec : DescribeSpec() {
  init {
    describe("Create") {
      it("should create new context") {
        val uuidFactory = mock(UuidFactory::class.java)
        val fixedUtcClock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
        val transactionId = "8521bc1a-31ae-4720-b78a-c0ff929b2b44"
        val messageId = "3f6555f9-96c7-4555-8000-29a06be3c931"
        `when`(uuidFactory.create()).thenReturn(transactionId, messageId)
        val contextFactory = ContextFactory(
          Domain.Delivery.value,
          City.Pune.value,
          Country.India.value,
          "bpp 1",
          "bpp1.com",
          uuidFactory,
          fixedUtcClock
        )

        val context = contextFactory.create()

        context.domain shouldBe Domain.Delivery.value
        context.country shouldBe Country.India.value
        context.city shouldBe City.Pune.value
        context.action shouldBe ProtocolContext.Action.SEARCH
        context.coreVersion shouldBe ProtocolVersion.V0_9_1.value
        context.bapId shouldBe "bap 1"
        context.bapUri shouldBe "bap1.com"
        context.bppId shouldBe null
        context.bppUri shouldBe null
        context.transactionId shouldBe transactionId
        context.messageId shouldBe messageId
        context.clock shouldBe fixedUtcClock
        context.key shouldBe null
        context.ttl shouldBe null
      }
    }
  }
}
