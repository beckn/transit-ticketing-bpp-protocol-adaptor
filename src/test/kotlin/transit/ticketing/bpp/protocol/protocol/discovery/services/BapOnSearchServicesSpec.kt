package transit.ticketing.bpp.protocol.protocol.discovery.services

import arrow.core.Either
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.common.factories.ContextFactoryInstance
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork
import transit.ticketing.bpp.protocol.protocol.common.factories.SubscriberDtoFactory
import transit.ticketing.bpp.protocol.protocol.external.provider.BapServiceClient
import transit.ticketing.bpp.protocol.protocol.external.provider.BapServiceFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class BapOnSearchServicesSpec @Autowired constructor() : DescribeSpec() {
    private val bapServiceFactory = Mockito.mock(BapServiceFactory::class.java)
    private val bapOnSearchService = BapOnSearchService(
        bapServiceFactory = bapServiceFactory
    )
    private val bapServiceClient: BapServiceClient = Mockito.mock(BapServiceClient::class.java)

    private val context = ContextFactoryInstance.create().create(bapId = "swtd_bap_id")
    private val gateway = SubscriberDtoFactory.getDefault(number = 1)
    private val successResponse = ProtocolOnSearch(context, ProtocolOnSearchMessage(catalog = ProtocolCatalog()))

    init {

        describe("SearchService") {
            MockNetwork.startAllSubscribers()
            beforeEach {
                Mockito.reset(bapServiceClient)
            }
            it("should invoke search and when on-search success"){
                Mockito.`when`(bapServiceFactory.getBapClient(any())).thenReturn(bapServiceClient)
                Mockito.`when`(bapServiceClient.onSearch(any())).thenReturn(ProtocolAckResponse(context,ResponseMessage.ack()))
                    val response = invokeBapService(subscriberDto = gateway,context= context,protocolOnSearch = successResponse)
                    response.isRight().shouldBeTrue()
                    verify(bapServiceFactory,Mockito.times(1)).getBapClient(any())
                    verifyBlocking(bapServiceClient,Mockito.times(1)){
                        onSearch(successResponse)
                    }
            }

            it("should invoke search and when on-search return null"){
                Mockito.`when`(bapServiceFactory.getBapClient(any())).thenReturn(bapServiceClient)
                Mockito.`when`(bapServiceClient.onSearch(any())).thenReturn(ProtocolAckResponse(context,message = ResponseMessage.nack(),error = BppError.NullResponse.nullError))
                val response = invokeBapService(subscriberDto = gateway,context= context,protocolOnSearch = successResponse)
                response.isLeft().shouldBeTrue()
            }

            it("should invoke search and when on-search  throws exception"){
                Mockito.`when`(bapServiceFactory.getBapClient(any())).thenReturn(bapServiceClient)
                doThrow(IllegalStateException("Error occurred")).`when`(bapServiceClient).onSearch( any())
                        val response = invokeBapService(subscriberDto = gateway,context= context,protocolOnSearch = successResponse)
                response.isLeft().shouldBeTrue()
            }

        }
    }

    private fun invokeBapService(subscriberDto: SubscriberDto,
                                 context: ProtocolContext,
                                 protocolOnSearch: ProtocolOnSearch):
            Either<BppError,ProtocolAckResponse> = runBlocking{
        bapOnSearchService.onSearch(subscriberDto,context,protocolOnSearch)
    }


}
