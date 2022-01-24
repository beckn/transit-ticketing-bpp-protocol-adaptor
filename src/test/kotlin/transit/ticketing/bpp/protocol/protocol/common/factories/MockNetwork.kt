package transit.ticketing.bpp.protocol.protocol.common.factories

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import transit.ticketing.bpp.protocol.protocol.common.City
import transit.ticketing.bpp.protocol.protocol.common.Country
import transit.ticketing.bpp.protocol.protocol.common.Domain
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto

object MockNetwork {

  val registryBppLookupApi = WireMockServer(4001)
  val retailBengaluruBpp = WireMockServer(4002)

  fun startAllSubscribers() {
    registryBppLookupApi.start()
    retailBengaluruBpp.start()
  }

  fun resetAllSubscribers() {
    registryBppLookupApi.resetAll()
    retailBengaluruBpp.resetAll()
  }

  fun getAllSubscribers(): List<SubscriberDto> {
    return listOf(
      getRetailBengaluruBpp(),
    )
  }

  fun getRetailBengaluruBpp() = createSubscriberDto(
    number = 4, mockServer = retailBengaluruBpp, type = SubscriberDto.Type.BPP
  )

  private fun createSubscriberDto(
    number: Int,
    mockServer: WireMockServer,
    type: SubscriberDto.Type = SubscriberDto.Type.BG,
    domain: String = Domain.LocalRetail.value,
    city: String = City.Bengaluru.value,
    country: String = Country.India.value,
    status: SubscriberDto.Status = SubscriberDto.Status.SUBSCRIBED
  ) = SubscriberDtoFactory.getDefault(
    number = number,
    baseUrl = mockServer.baseUrl(),
    type = type,
    domain = domain,
    city = city,
    country = country,
    status = status,
  )

  fun stubBppLookupApi(bppApi: WireMockServer, objectMapper: ObjectMapper) {
    registryBppLookupApi
      .stubFor(
        WireMock.post("/lookup")
          .withRequestBody(WireMock.matchingJsonPath("$.subscriber_id", WireMock.equalTo(bppApi.baseUrl())))
          .willReturn(WireMock.okJson(getSubscriberForBpp(objectMapper, bppApi)))
      )
  }

  private fun getSubscriberForBpp(objectMapper: ObjectMapper, bppApi: WireMockServer) =
    objectMapper.writeValueAsString(
      listOf(
        SubscriberDtoFactory.getDefault(
          subscriber_id = bppApi.baseUrl(),
          baseUrl = bppApi.baseUrl(),
          type = SubscriberDto.Type.BPP,
        )
      )
    )

}