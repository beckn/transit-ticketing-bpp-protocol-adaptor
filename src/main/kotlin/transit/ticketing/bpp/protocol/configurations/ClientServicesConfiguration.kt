package transit.ticketing.bpp.protocol.configurations


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import transit.ticketing.bpp.protocol.message.entities.OnConfirmDao
import transit.ticketing.bpp.protocol.message.entities.OnOrderStatusDao
import transit.ticketing.bpp.protocol.message.mappers.GenericResponseMapper
import transit.ticketing.bpp.protocol.message.repositories.BecknResponseRepository
import transit.ticketing.bpp.protocol.message.services.ResponseStorageService
import transit.ticketing.bpp.protocol.message.services.ResponseStorageServiceImpl
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnConfirm
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnOrderStatus

@Configuration
class ClientServicesConfiguration @Autowired constructor() {

  @Bean
  fun setConfirmOrderProtocolToDao(
    @Autowired responseRepository: BecknResponseRepository<OnConfirmDao>,
    @Autowired mapper: GenericResponseMapper<ProtocolOnConfirm, OnConfirmDao>,
  ): ResponseStorageService<ProtocolOnConfirm, OnConfirmDao> = ResponseStorageServiceImpl(responseRepository,mapper)

  @Bean
  fun setOrderStatusProtocolToDao(
    @Autowired responseRepository: BecknResponseRepository<OnOrderStatusDao>,
    @Autowired mapper: GenericResponseMapper<ProtocolOnOrderStatus, OnOrderStatusDao>,
  ): ResponseStorageService<ProtocolOnOrderStatus, OnOrderStatusDao> = ResponseStorageServiceImpl(responseRepository,mapper)

}