package transit.ticketing.bpp.protocol.protocol.discovery.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork.registryBppLookupApi
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork.retailBengaluruBpp
import transit.ticketing.bpp.protocol.protocol.common.factories.ResponseFactory
import transit.ticketing.bpp.protocol.protocol.shared.dtos.ClientContext
import transit.ticketing.bpp.protocol.protocol.shared.dtos.SearchRequestDto
import transit.ticketing.bpp.protocol.protocol.shared.dtos.SearchRequestMessageDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolFulfillment
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolIntent
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseStatus
import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory
import transit.ticketing.bpp.protocol.schemas.factories.UuidFactory

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class SearchControllerSpec @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper
) : DescribeSpec() {
    init {

        describe("SearchController") {
            MockNetwork.startAllSubscribers()
            beforeEach {
                MockNetwork.resetAllSubscribers()
            }

            it("should return error response when registry lookup fails") {
                registryBppLookupApi
                    .stubFor(post("/lookup").willReturn(serverError()))

                invokeSearchApi()
                    .andExpect(status().is5xxServerError)
                    .andExpect(jsonPath("$.message.ack.status", `is`("NACK")))
                    .andExpect(jsonPath("$.error.code", `is`("BPP_001")))
                    .andExpect(jsonPath("$.error.message", `is`("Registry lookup returned error")))
            }

            it("should invoke Client /search API on first  and fail") {
                stubLookupApi()
                val result: MvcResult = invokeSearchApi()
                    .andExpect(status().is5xxServerError)
                    .andExpect(jsonPath("$.message.ack.status", `is`(ResponseStatus.NACK.status)))
                    .andExpect(jsonPath("$.context.message_id", `is`(notNullValue())))
                    .andReturn()
            }

            it("should invoke Client /search API on first  and success") {
                stubLookupApi()
                stubBppClientApi()
                val result: MvcResult = invokeSearchApi()
                    .andExpect(status().is2xxSuccessful)
                    .andExpect(jsonPath("$.message.ack.status", `is`(ResponseStatus.ACK.status)))
                    .andExpect(jsonPath("$.context.message_id", `is`(notNullValue())))
                    .andReturn()

                verifyThatSearchApiWasInvoked()
                verifyAckResponse(result)
            }
        }
    }


    private fun verifyAckResponse(result: MvcResult): ProtocolAckResponse {
        val searchResponse = objectMapper.readValue(result.response.contentAsString, ProtocolAckResponse::class.java)
        searchResponse.message.ack.status shouldBe ResponseStatus.ACK
        return searchResponse
    }

    private fun verifyThatSearchApiWasInvoked() {
        retailBengaluruBpp.verify(getRequestedFor(urlPathMatching("/search.*")))
    }

    private fun invokeSearchApi(transactionId: String = "123456789", bapId: String = "12"): ResultActions {
        val searchRequestDto = SearchRequestDto(
            context = ClientContext(transactionId, bapId),
            message = SearchRequestMessageDto(
                intent = ProtocolIntent(
                    fulfillment = ProtocolFulfillment()
                )
            )
        )
        return mockMvc
            .perform(
                MockMvcRequestBuilders.post("/protocol/v1/search")
                    .content(objectMapper.writeValueAsString(searchRequestDto))
                    .contentType(MediaType.APPLICATION_JSON)
            )
    }

    private fun stubLookupApi() {
        val subsriberJson = objectMapper.writeValueAsString(MockNetwork.getAllSubscribers())
        registryBppLookupApi
            .stubFor(post("/lookup").willReturn(okJson(subsriberJson)))
    }

    private fun stubBppClientApi() {
        retailBengaluruBpp
            .stubFor(
                get(urlPathMatching("/search.*")).willReturn(
                    okJson(
                        objectMapper.writeValueAsString(ResponseFactory.getSearchResponseFromBpp())
                    )
                )
            )
    }
}
