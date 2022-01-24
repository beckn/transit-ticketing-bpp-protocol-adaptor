package transit.ticketing.bpp.protocol.protocol.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import transit.ticketing.bpp.protocol.protocol.external.domains.Subscriber
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberLookupRequest
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolError
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseMessage

class Verifier(
  private val objectMapper: ObjectMapper
) {

  fun verifyResponseMessage(
    responseString: String,
    expectedMessage: ResponseMessage,
    expectedError: ProtocolError? = null,
    action: ProtocolContext.Action
  ): ProtocolAckResponse {
    val getQuoteResponse = objectMapper.readValue(responseString, ProtocolAckResponse::class.java)
    getQuoteResponse.context shouldNotBe null
    getQuoteResponse.context?.messageId shouldNotBe null
    getQuoteResponse.context?.action shouldBe action
    getQuoteResponse.message shouldBe expectedMessage
    getQuoteResponse.error shouldBe expectedError
    return getQuoteResponse
  }

  fun verifyThatSubscriberLookupApiWasInvoked(
    registryBppLookupApi: WireMockServer,
    bppApi: WireMockServer
  ) {
    registryBppLookupApi.verify(
      postRequestedFor(urlEqualTo("/lookup"))
        .withRequestBody(
          equalToJson(
            objectMapper.writeValueAsString(
              SubscriberLookupRequest(
                subscriber_id = bppApi.baseUrl(),
                type = Subscriber.Type.BPP,
                domain = Domain.LocalRetail.value,
                country = Country.India.value,
                city = City.Bengaluru.value
              )
            )
          )
        )
    )
  }

}