package transit.ticketing.bpp.protocol.protocol.confirm.services

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.errors.database.DatabaseError
import transit.ticketing.bpp.protocol.errors.registry.RegistryLookupError
import transit.ticketing.bpp.protocol.message.entities.OnConfirmDao
import transit.ticketing.bpp.protocol.message.entities.ProtocolOnConfirmMessageDao
import transit.ticketing.bpp.protocol.message.entities.ProtocolOrderDao
import transit.ticketing.bpp.protocol.message.mappers.GenericResponseMapper
import transit.ticketing.bpp.protocol.message.services.ResponseStorageService
import transit.ticketing.bpp.protocol.protocol.common.factories.ContextFactoryInstance
import transit.ticketing.bpp.protocol.protocol.common.factories.MockNetwork
import transit.ticketing.bpp.protocol.protocol.common.factories.ResponseFactory.Companion.getConfirmFromBpp
import transit.ticketing.bpp.protocol.protocol.common.factories.SubscriberDtoFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.dtos.ConfirmRequestMessageDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService
import transit.ticketing.bpp.protocol.protocol.status.services.StatusService
import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class ConfirmServicesSpec @Autowired constructor(
    val objectMapper: ObjectMapper,
    val contextFactory: ContextFactory,
    private val mapper: GenericResponseMapper<ProtocolOnConfirm, OnConfirmDao>,

    ) : DescribeSpec() {

    private val registryService = Mockito.mock(RegistryService::class.java)
    private val bppClientConfirmService=  Mockito.mock(BppClientConfirmService::class.java)
    private val bapOnConfirmService= Mockito.mock( BapOnConfirmService::class.java)
    private val context = ContextFactoryInstance.create().create(transactionId = "123456789",bapId = "swtd_bap_id")
    private val protocolConfirmResponse = getConfirmFromBpp(context)
    private val mockMapper = mock<GenericResponseMapper<ProtocolOnConfirm, OnConfirmDao>>{
        this.onGeneric {  entityToProtocol(any()) }.thenReturn(protocolConfirmResponse)
    }
    private val successResponseStorage = mock<ResponseStorageService<ProtocolOnConfirm, OnConfirmDao>>{
        this.onGeneric {  updateDocByQuery(any(),any()) }.thenReturn(Either.Right(protocolConfirmResponse))
    }
    private val failResponseStorage = mock<ResponseStorageService<ProtocolOnConfirm, OnConfirmDao>>{
        this.onGeneric {  updateDocByQuery(any(),any()) }.thenReturn(Either.Left(DatabaseError.OnWrite))
    }
    val successResponse = mapper.protocolToEntity(getConfirmFromBpp(context))
    private val confirmService = ConfirmService(
        registryService = registryService,
        bppClientConfirmService = bppClientConfirmService,
        bapOnConfirmService = bapOnConfirmService,
        mapper = mockMapper,
        confirmRepository = successResponseStorage
    )
    private val gateway = SubscriberDtoFactory.getDefault(number = 1)
    private val otherGateway = SubscriberDtoFactory.getDefault(number = 2)
    private val registeryInternalError = RegistryLookupError.Internal
    private val registerySubscriberError = RegistryLookupError.NoSubscriberFound
    private val nackError = BppError.Nack
    private val badRequestError = BppError.BadRequestError

    init {

        describe("ConfirmService") {
            MockNetwork.startAllSubscribers()
            beforeEach {
                Mockito.reset(registryService)
                Mockito.reset(bppClientConfirmService)
                Mockito.reset(bapOnConfirmService)
            }


            it("should invoke confirm and return bad request due to no item list") {
                val request = ConfirmRequestMessageDto(
                    order = ProtocolOrder(
                        fulfillment = ProtocolFulfillment()
                    )
                )
                stubLookupApiFail()
                val searchResponse = confirmService.confirm(context,request )
                searchResponse shouldBeLeft  badRequestError
            }
            it("should invoke confirm and return bad request due to no fullfilment id list") {
                val request = ConfirmRequestMessageDto(
                    order = ProtocolOrder(
                        fulfillment = ProtocolFulfillment(),
                        items = listOf(ProtocolItem(id = "1"))
                    )
                )
                stubLookupApiFail()
                val searchResponse = confirmService.confirm(context,request )
                searchResponse shouldBeLeft  badRequestError
            }

            it("should invoke confirm and throw error due to look up api ") {

                val request = ConfirmRequestMessageDto(
                    order = protocolConfirmResponse.message?.order!!
                )
                stubLookupApiFail()
                val searchResponse = confirmService.confirm(context,request )
                searchResponse shouldBeLeft  registeryInternalError
            }
//            it("should invoke lookup api and return error") {
//                val request = ConfirmRequestMessageDto(
//                    order = ProtocolOrder(
//                        fulfillment = ProtocolFulfillment()
//                    )
//                )
//                stubLookupApiFail()
//                val searchResponse = confirmService.confirm(context,request )
//                searchResponse shouldBeLeft  registeryInternalError
//            }
//
//            it("should invoke lookup api and return empty list and throws no subscriber found") {
//                Mockito.`when`(registryService.lookupBapById(any())).thenReturn(Either.Right(listOf()))
//                val searchResponse = confirmService.confirm(context, searchMessage.intent)
//                searchResponse shouldBeLeft  registerySubscriberError
//            }
//
//            it("should invoke lookup api and return success subscriber list ") {
//                stubLookupApiSuccess(gateway= gateway,anotherGateway = otherGateway)
//                stubClientBapApiSuccess(gateway,searchMessage.intent)
//                val searchResponse = confirmService.confirm(context, searchMessage.intent)
//                searchResponse.isRight().shouldBeTrue()
//                verifyClientSearchApiInvoked(gateway,searchMessage.intent,1)
//            }
        }
    }

    private fun verifyClientSearchApiInvoked(gateway: SubscriberDto ,request: ConfirmRequestMessageDto, numberOfTimes: Int = 1) {
        Mockito.verify(bppClientConfirmService, Mockito.times(numberOfTimes)).blockTicket(gateway, context, request)
    }

    private fun stubClientBapApiSuccess(gateway: SubscriberDto ) = runBlocking{
        Mockito.`when`(bppClientConfirmService.blockTicket(any(), any(), any())).thenReturn(Either.Right(successResponse))
        Mockito.`when`(bapOnConfirmService.postOnConfirm(any(), any(), any())).thenReturn(Either.Left(BppError.NullResponse))
    }

    private fun stubLookupApiFail() {
        Mockito.`when`(registryService.lookupBapById(any())).thenReturn(Either.Left(registeryInternalError))
    }
    private fun stubLookupApiSuccess(gateway: SubscriberDto, anotherGateway: SubscriberDto) {
        Mockito.`when`(registryService.lookupBapById(any())).thenReturn(Either.Right(listOf(gateway, anotherGateway)))
    }

}
