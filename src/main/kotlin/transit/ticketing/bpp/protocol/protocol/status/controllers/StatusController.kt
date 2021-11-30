package transit.ticketing.bpp.protocol.protocol.status.controllers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import transit.ticketing.bpp.protocol.protocol.status.services.StatusService
import transit.ticketing.bpp.protocol.protocol.shared.schemas.*
import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory

@RestController
class StatusController @Autowired constructor(
  val confirmService: StatusService,
  val contextFactory: ContextFactory
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/protocol/v1/status")
  @ResponseBody
  fun confirmV1(@RequestBody request: ProtocolOrderStatusRequest): ResponseEntity<ProtocolAckResponse> {
    val protocolContext =
      contextFactory.create(transactionId = request.context.transactionId, action = ProtocolContext.Action.SEARCH,
        bapId = request.context.bapId)
    return confirmService.postOrderRequest(protocolContext, request.message)
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