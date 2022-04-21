package transit.ticketing.bpp.protocol.protocol.status.controllers

import arrow.core.Either
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.mockito.Mock
import org.mockito.kotlin.mock
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
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.message.entities.OnOrderStatusDao
import transit.ticketing.bpp.protocol.message.services.ResponseStorageService
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork.registryBppLookupApi
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork.retailBengaluruBpp
import transit.ticketing.bpp.protocol.protocol.common.factories.ResponseFactory
import transit.ticketing.bpp.protocol.protocol.common.factories.ResponseFactory.Companion.getStatusFromBpp
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.dtos.OrderStatusRequestDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnOrderStatus
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseStatus
import transit.ticketing.bpp.protocol.protocol.status.services.StatusService
import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class StatusControllerSpec @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val contextFactory: ContextFactory
) : DescribeSpec() {

    init {

        describe("StatusController") {
            MockNetwork.startAllSubscribers()
            beforeEach {
                MockNetwork.resetAllSubscribers()
            }

            it("should return error response when registry lookup fails") {
                registryBppLookupApi
                    .stubFor(post("/lookup").willReturn(serverError()))

                invokeStatusApi()
                    .andExpect(status().is5xxServerError)
                    .andExpect(jsonPath("$.error.code", `is`("BPP_001")))
                    .andExpect(jsonPath("$.error.message", `is`("Registry lookup returned error")))
            }

            it("should return error response when registry lookup  fails bad request") {
                registryBppLookupApi
                    .stubFor(post("/lookup").willReturn(badRequest()))

                invokeStatusApi()
                    .andExpect(status().is5xxServerError)
                    .andExpect(jsonPath("$.error.code", `is`("BPP_002")))
                    .andExpect(jsonPath("$.error.message", `is`("Registry lookup did not return any Subscribers")))
            }

            it("should invoke Client /status db on first and gets fail") {
                stubLookupApi()
                invokeStatusApi()
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("$.error.code", `is`("BPP_008")))
                    .andExpect(jsonPath("$.error.message", `is`("No message with the given ID")))
                    .andExpect(jsonPath("$.error.message", `is`("No message with the given ID")))
                    .andExpect(jsonPath("$.context.message_id", `is`(notNullValue())))
                    .andReturn()
            }

            context("should invoke Client /status db on first and return success") {
                val context = contextFactory.create(transactionId = "123456789")
                val currentDateTimeInMS = Util.dateToMiliseconds(Util.getCurrentDateInString())
                val mockResponse  = getStatusFromBpp(context,currentDateTimeInMS?:"")
                val statusRequest = OrderStatusRequestDto(
                    context = context
                )
                val statusService= mock<StatusService>{
                    this.onGeneric { getOrderStatusRequest(context) }.thenReturn(Either.Right(mockResponse))
                }
                val controller = StatusController(statusService)
                it("should respond with success") {
                    val results = controller.statusV1(statusRequest)
                    results.statusCode.is2xxSuccessful
                    results.body shouldNotBe null
                    results.body?.message shouldNotBe null
                    results.body?.error shouldBe  null
                }
            }
        }

        context("should invoke Client /payment first and fail") {
            val context = contextFactory.create(transactionId = "123456789")
            val currentDateTimeInMS = Util.dateToMiliseconds(Util.getCurrentDateInString())
            val statusService= mock<StatusService>{
                this.onGeneric { getPaymentDetails(transactionId = context.transactionId) }.thenReturn(Either.Left(BppError.Internal))
            }
            val controller = StatusController(statusService)
            it("should respond with badrequest error") {
                val results = controller.paymentV1(transactionId = "")
                results.statusCode.is4xxClientError
                results.body shouldNotBe  null
                results.body?.message shouldBe  null
                results.body?.error shouldNotBe  null
            }
            it("should respond with error") {
                val results = controller.paymentV1(transactionId = context.transactionId)
                results.statusCode.is5xxServerError
                results.body shouldNotBe  null
                results.body?.message shouldBe  null
                results.body?.error shouldNotBe  null
            }
        }

        context("should invoke Client /payment first and success") {
            val context = contextFactory.create(transactionId = "123456789")
            val currentDateTimeInMS = Util.dateToMiliseconds(Util.getCurrentDateInString())
            val mockResponse  = getStatusFromBpp(context,currentDateTimeInMS?:"")
            val statusService= mock<StatusService>{
                this.onGeneric { getPaymentDetails(transactionId = context.transactionId) }.thenReturn(Either.Right(mockResponse))
            }
            val controller = StatusController(statusService)
            it("should respond with success") {
                val results = controller.paymentV1(context.transactionId)
                results.statusCode.is2xxSuccessful
                results.body shouldNotBe null
                results.body?.message shouldNotBe null
                results.body?.error shouldBe  null
            }
        }
    }


    private fun verifyAckResponse(result: MvcResult): ProtocolAckResponse {
        val searchResponse = objectMapper.readValue(result.response.contentAsString, ProtocolAckResponse::class.java)
        searchResponse.message.ack.status shouldBe ResponseStatus.ACK
        return searchResponse
    }

    private fun invokeStatusApi(transactionId: String = "123456789", bapId: String = "12"): ResultActions {
        val statusRequest = OrderStatusRequestDto(
            context = contextFactory.create(transactionId= transactionId,bapId = bapId)
        )
        return mockMvc
            .perform(
                MockMvcRequestBuilders.post("/protocol/v1/status")
                    .content(objectMapper.writeValueAsString(statusRequest))
                    .contentType(MediaType.APPLICATION_JSON)
            )
    }

    private fun stubLookupApi() {
        val subsriberJson = objectMapper.writeValueAsString(MockNetwork.getAllSubscribers())
        registryBppLookupApi
            .stubFor(post("/lookup").willReturn(okJson(subsriberJson)))
    }

}
