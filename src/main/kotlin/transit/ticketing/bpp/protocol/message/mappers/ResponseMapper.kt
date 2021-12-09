package transit.ticketing.bpp.protocol.message.mappers

import org.mapstruct.InjectionStrategy
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.springframework.stereotype.Component
import transit.ticketing.bpp.protocol.message.entities.BecknResponseDao
import transit.ticketing.bpp.protocol.message.entities.OnConfirmDao
import transit.ticketing.bpp.protocol.message.entities.OnOrderStatusDao
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnConfirm
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnOrderStatus
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolResponse
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId


interface GenericResponseMapper<Protocol : ProtocolResponse, Entity : BecknResponseDao> {
  fun entityToProtocol(entity: Entity): Protocol
  fun protocolToEntity(schema: Protocol): Entity
}

@Component
class DateMapper {
  fun map(instant: Instant?): OffsetDateTime? {
    return instant?.let { OffsetDateTime.ofInstant(it, ZoneId.of("UTC")) }
  }

  fun map(offset: OffsetDateTime?): Instant? {
    return offset?.toInstant()
  }
}

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.WARN,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  uses = [DateMapper::class]
)
interface OnConfirmResponseMapper : GenericResponseMapper<ProtocolOnConfirm, OnConfirmDao> {
  override fun entityToProtocol(entity: OnConfirmDao): ProtocolOnConfirm

  override fun protocolToEntity(schema: ProtocolOnConfirm): OnConfirmDao
}
@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.WARN,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  uses = [DateMapper::class]
)
interface OnOrderStatusResponseMapper : GenericResponseMapper<ProtocolOnOrderStatus, OnOrderStatusDao> {
  override fun entityToProtocol(entity: OnOrderStatusDao): ProtocolOnOrderStatus

  override fun protocolToEntity(schema: ProtocolOnOrderStatus): OnOrderStatusDao
}
