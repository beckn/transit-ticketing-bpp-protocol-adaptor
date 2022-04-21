package transit.ticketing.bpp.protocol.protocol.confirm.controllers

import com.fasterxml.jackson.databind.ObjectMapper
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
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.Util.getCurrentDateInString
import transit.ticketing.bpp.protocol.protocol.shared.dtos.*
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory
import transit.ticketing.bpp.protocol.schemas.factories.UuidFactory

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class ConfirmControllerSpec @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val contextFactory: ContextFactory,
    val uuidFactory: UuidFactory
) : DescribeSpec() {
    init {
        describe("ConfirmController") {
            MockNetwork.startAllSubscribers()
            beforeEach {
                MockNetwork.resetAllSubscribers()
            }

            it("should return error response when send incorrect request") {
                invokeWithInCorrectRequestConfirmApi()
                    .andExpect(status().is4xxClientError)
                    .andExpect(jsonPath("$.message.ack.status", `is`("NACK")))
                    .andExpect(jsonPath("$.error.code", `is`("BPP_404")))
                    .andExpect(jsonPath("$.error.message", `is`("Bad Request")))
            }

            it("should return error response when registry fails") {
                registryBppLookupApi
                    .stubFor(post("/lookup").willReturn(serverError()))

                invokeConfirmApi()
                    .andExpect(status().is5xxServerError)
                    .andExpect(jsonPath("$.message.ack.status", `is`("NACK")))
                    .andExpect(jsonPath("$.error.code", `is`("BPP_001")))
                    .andExpect(jsonPath("$.error.message", `is`("Registry lookup returned error")))
            }

            it("should invoke Client /confirm API on first and gets fail") {
                stubLookupApi()
                val result: MvcResult = invokeConfirmApi()
                    .andExpect(status().is5xxServerError)
                    .andExpect(jsonPath("$.message.ack.status", `is`(ResponseStatus.NACK.status)))
                    .andExpect(jsonPath("$.context.message_id", `is`(notNullValue())))
                    .andReturn()
            }

            it("should invoke Client /confirm API on first and success") {
                stubLookupApi()
                stubBppClientApi()
                val result: MvcResult = invokeConfirmApi()
                    .andExpect(status().is2xxSuccessful)
                    .andExpect(jsonPath("$.message.ack.status", `is`(ResponseStatus.ACK.status)))
                    .andExpect(jsonPath("$.context.message_id", `is`(notNullValue())))
                    .andReturn()

                verifyThatBlockApiWasInvoked()
                verifyAckResponse(result)
            }
        }
    }


    private fun verifyAckResponse(result: MvcResult): ProtocolAckResponse {
        val searchResponse = objectMapper.readValue(result.response.contentAsString, ProtocolAckResponse::class.java)
        searchResponse.message.ack.status shouldBe ResponseStatus.ACK
        return searchResponse
    }

    private fun verifyThatBlockApiWasInvoked() {
        retailBengaluruBpp.verify(postRequestedFor(urlPathMatching("/block_ticket")))
    }

    private fun invokeWithInCorrectRequestConfirmApi(transactionId: String = "123456789", bapId: String = "12"): ResultActions {
        val confirmRequestDto = ConfirmRequestDto(
            context = contextFactory.create(transactionId=transactionId,bapId=bapId),
            message = ConfirmRequestMessageDto(
                order = ProtocolOrder(
                    fulfillment = ProtocolFulfillment()
                )
            )
        )
        return mockMvc
            .perform(
                MockMvcRequestBuilders.post("/protocol/v1/confirm")
                    .content(objectMapper.writeValueAsString(confirmRequestDto))
                    .contentType(MediaType.APPLICATION_JSON)
            )
    }

    private fun invokeConfirmApi(transactionId: String = "123456789", bapId: String = "12"): ResultActions {
        val currentDateTimeInMS = Util.dateToMiliseconds(getCurrentDateInString())

        val confirmRequestDto = ConfirmRequestDto(
            context = contextFactory.create(transactionId=transactionId,bapId=bapId),
            message = ConfirmRequestMessageDto(
                order = ProtocolOrder(
                    provider = ProtocolSelectMessageSelectedProvider(
                        id = "KSWTD"
                    ),
                    items= listOf(ProtocolItem(
                        id = "ONE_WAY_TICKET",
                        fulfillmentId = "2001-$currentDateTimeInMS-$currentDateTimeInMS-100-101",
                        descriptor = ProtocolDescriptor(name = "Kochi"),
                        price = ProtocolPrice(currency = "INR",value = "122"),
                        quantity = ProtocolItemQuantityAllocated(count = 1)
                    )),
                    fulfillment = ProtocolFulfillment(
                        id = "2001-$currentDateTimeInMS-$currentDateTimeInMS-100-101",
                        start = ProtocolFulfillmentStart(),
                        end = ProtocolFulfillmentEnd()
                    )
                )
            )
        )
        return mockMvc
            .perform(
                MockMvcRequestBuilders.post("/protocol/v1/confirm")
                    .content(objectMapper.writeValueAsString(confirmRequestDto))
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
                post(urlEqualTo("/block_ticket")).willReturn(
                    okJson(
                        objectMapper.writeValueAsString(ResponseFactory.getConfirmResponseFromBpp())
                    )
                )
            )
    }
}
