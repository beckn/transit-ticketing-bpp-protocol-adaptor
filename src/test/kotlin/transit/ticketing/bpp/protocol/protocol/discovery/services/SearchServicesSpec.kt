package transit.ticketing.bpp.protocol.protocol.discovery.services

import arrow.core.Either
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.errors.registry.RegistryLookupError
import transit.ticketing.bpp.protocol.protocol.common.factories.ContextFactoryInstance
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork
import transit.ticketing.bpp.protocol.protocol.common.factories.SubscriberDtoFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.dtos.SearchRequestMessageDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class SearchServicesSpec @Autowired constructor() : DescribeSpec() {

    private val registryService = Mockito.mock(RegistryService::class.java)
    private val bppClientSearchService=  Mockito.mock(BppClientSearchService::class.java)
    private val bapOnSearchService= Mockito.mock( BapOnSearchService::class.java)
    private val searchService = SearchService(
        registryService = registryService,
        bppClientSearchService = bppClientSearchService,
        bapOnSearchService = bapOnSearchService
    )
    private val context = ContextFactoryInstance.create().create(bapId = "swtd_bap_id")
    private val gateway = SubscriberDtoFactory.getDefault(number = 1)
    private val otherGateway = SubscriberDtoFactory.getDefault(number = 2)
    private val successResponse = ProtocolOnSearch(context, ProtocolOnSearchMessage(catalog = ProtocolCatalog()))
    private val registeryInternalError = RegistryLookupError.Internal
    private val registerySubscriberError = RegistryLookupError.NoSubscriberFound
    private val nackError = BppError.Nack
    val searchMessage = SearchRequestMessageDto(
        intent = ProtocolIntent(
            fulfillment = ProtocolFulfillment()
        )
    )
    init {

        describe("SearchService") {
            MockNetwork.startAllSubscribers()
            beforeEach {
                Mockito.reset(registryService)
                Mockito.reset(bppClientSearchService)
                Mockito.reset(bapOnSearchService)
            }

            it("should invoke lookup api and return error") {
                stubLookupApiFail()
                val searchResponse = searchService.search(context, searchMessage.intent)
                searchResponse shouldBeLeft  registeryInternalError
            }

            it("should invoke lookup api and return empty list and throws no subscriber found") {
                Mockito.`when`(registryService.lookupBapById(any())).thenReturn(Either.Right(listOf()))
                val searchResponse = searchService.search(context, searchMessage.intent)
                searchResponse shouldBeLeft  registerySubscriberError
            }

            it("should invoke lookup api and return success subscriber list ") {
                stubLookupApiSuccess(gateway= gateway,anotherGateway = otherGateway)
                stubClientBapApiSuccess(gateway,searchMessage.intent)
                val searchResponse = searchService.search(context, searchMessage.intent)
                searchResponse.isRight().shouldBeTrue()
                verifyClientSearchApiInvoked(gateway,searchMessage.intent,1)
            }
        }
    }

    private fun verifyClientSearchApiInvoked(gateway: SubscriberDto ,intent: ProtocolIntent, numberOfTimes: Int = 1) {
        Mockito.verify(bppClientSearchService, Mockito.times(numberOfTimes)).search(gateway, context, intent)
    }

    private fun stubClientBapApiSuccess(gateway: SubscriberDto ,intent: ProtocolIntent) = runBlocking{
        Mockito.`when`(bppClientSearchService.search(any(), any(), any())).thenReturn(Either.Right(successResponse))
        Mockito.`when`(bapOnSearchService.onSearch(any(), any(), any())).thenReturn(Either.Left(BppError.NullResponse))
    }

    private fun stubLookupApiFail() {
        Mockito.`when`(registryService.lookupBapById(any())).thenReturn(Either.Left(RegistryLookupError.Internal))
    }
    private fun stubLookupApiSuccess(gateway: SubscriberDto, anotherGateway: SubscriberDto) {
        Mockito.`when`(registryService.lookupBapById(any())).thenReturn(Either.Right(listOf(gateway, anotherGateway)))
    }

}
