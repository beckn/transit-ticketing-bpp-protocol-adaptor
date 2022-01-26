package transit.ticketing.bpp.protocol.protocol.discovery.services

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType.APPLICATION_JSON
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.runBlocking
import org.apache.http.HttpStatus
import org.apache.http.entity.ContentType.APPLICATION_JSON
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.shaded.okhttp3.MediaType
import org.testcontainers.shaded.okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.HTTP
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.common.factories.ContextFactoryInstance
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork.retailBengaluruBpp
import transit.ticketing.bpp.protocol.protocol.common.factories.ResponseFactory
import transit.ticketing.bpp.protocol.protocol.common.factories.SubscriberDtoFactory
import transit.ticketing.bpp.protocol.protocol.discovery.mappers.ProtocolOnSearchFactory
import transit.ticketing.bpp.protocol.protocol.external.provider.BppClientFactory
import transit.ticketing.bpp.protocol.protocol.external.provider.BppServiceClient
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.SearchResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.Trip
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class BppClientSearchServiceSpec @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper
) : DescribeSpec() {
    private val protocolSearchFactory = Mockito.mock(ProtocolOnSearchFactory::class.java)
    private val bppClientFactory = Mockito.mock(BppClientFactory::class.java)
    private val bppServiceClient = Mockito.mock(BppServiceClient::class.java)
    private val bppClientSearchService = BppClientSearchService(
        bppServiceClientFactory = bppClientFactory,
        clientUrl = "http://localhost:4002/",
        protocolOnSearchFactory = protocolSearchFactory
    )
    private val context = ContextFactoryInstance.create().create(bapId = "swtd_bap_id")
    private val gateway = SubscriberDtoFactory.getDefault(number = 1)
    private val protocolIntent = ProtocolIntent(
        fulfillment = ProtocolFulfillment(
            id = "2001-2002",
            start = ProtocolFulfillmentStart(
                location = ProtocolLocation(
                    id = "111",
                    gps = "100.0000,99.000"
                )
            ),
            end = ProtocolFulfillmentEnd(
                location = ProtocolLocation(
                    id = "111",
                    gps = "100.0000,99.000"
                )
            )
        )
    )

    init {

        describe("BppClientSearchService") {

            MockNetwork.startAllSubscribers()
            beforeEach {
                MockNetwork.resetAllSubscribers()
                Mockito.reset(bppServiceClient)
            }

            it("should invoke bpp client Search and throws bpp internal error"){
                Mockito.`when`(bppClientFactory.getClient(any())).thenReturn(bppServiceClient)
                val response = invokeBppService(subscriberDto = gateway,context= context,protocolIntent = protocolIntent)
                response.isLeft().shouldBeTrue()
            }

            it("should invoke bpp client Search and success"){
                Mockito.`when`(bppClientFactory.getClient(any())).thenReturn(bppServiceClient)
                val mockCall = mock<Call<SearchResponse>> {
                    on { execute() } doReturn Response.success(ResponseFactory.getSearchResponseFromBpp())
                }
                Mockito.`when`(bppServiceClient.search(any(),any())).thenReturn(mockCall)

                val response = invokeBppService(subscriberDto = gateway,context= context,protocolIntent = protocolIntent)
                response.isRight().shouldBeTrue()
            }

            it("should invoke bpp client Search and has null body"){
                Mockito.`when`(bppClientFactory.getClient(any())).thenReturn(bppServiceClient)
                val mockCall = mock<Call<SearchResponse>> {
                    on { execute() } doReturn Response.success(null)
                }
                Mockito.`when`(bppServiceClient.search(any(),any())).thenReturn(mockCall)

                val response = invokeBppService(subscriberDto = gateway,context= context,protocolIntent = protocolIntent)
                response.isLeft().shouldBeTrue()
            }
        }
    }

    private fun invokeBppService(subscriberDto: SubscriberDto,
                                 context: ProtocolContext,
                                 protocolIntent: ProtocolIntent):
            Either<BppError, ProtocolOnSearch> {
        return bppClientSearchService.search(subscriberDto,context,protocolIntent)
    }

}
