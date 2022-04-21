package transit.ticketing.bpp.protocol.protocol.init.controllers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import transit.ticketing.bpp.protocol.protocol.init.services.InitService
import transit.ticketing.bpp.protocol.protocol.shared.dtos.InitRequestDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseMessage
import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory

@RestController
class InitController @Autowired constructor(
    val initService: InitService,
    val contextFactory: ContextFactory
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/protocol/v1/init")
  @ResponseBody
  fun initV1(@RequestBody request: InitRequestDto): ResponseEntity<ProtocolAckResponse> {
    val protocolContext =
      contextFactory.create(transactionId = request.context.transactionId, action = ProtocolContext.Action.SEARCH,
        bapId = request.context.bapId)
    return initService.postInit(protocolContext, request.message)
      .fold(
        {
          log.error("Error during init request. Error: {}", it)
          ResponseEntity
            .status(it.status().value())
            .body(ProtocolAckResponse(protocolContext, it.message(), it.error()))
        },
        {
          log.info("Successfully initiated Search")
          ResponseEntity.ok(ProtocolAckResponse(protocolContext, ResponseMessage.ack()))
        }
      )
  }
}