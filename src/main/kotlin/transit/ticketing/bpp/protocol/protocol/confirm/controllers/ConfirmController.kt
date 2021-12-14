package transit.ticketing.bpp.protocol.protocol.confirm.controllers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.message.entities.OnConfirmDao
import transit.ticketing.bpp.protocol.protocol.confirm.services.ConfirmService
import transit.ticketing.bpp.protocol.protocol.shared.dtos.ConfirmRequestDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory

@RestController
class ConfirmController @Autowired constructor(
    val confirmService: ConfirmService,
    val contextFactory: ContextFactory
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/protocol/v1/confirm")
  @ResponseBody
  fun confirmV1(@RequestBody request: ConfirmRequestDto): ResponseEntity<ProtocolOnConfirm> {
    val protocolContext =
      contextFactory.create(transactionId = request.context.transactionId, action = ProtocolContext.Action.SEARCH,
        bapId = request.context.bapId)
    return confirmService.postConfirmRequest(protocolContext, request.message)
      .fold(
        {
          log.error("Error during init request. Error: {}", it)
          mapToErrorResponse(it,protocolContext)
        },
        {
          log.info("Successfully initiated Search")
          ResponseEntity.ok(it)
        }
      )
  }
  private fun mapToErrorResponse(it: HttpError, context: ProtocolContext? = null) = ResponseEntity
    .status(it.status())
    .body(
        ProtocolOnConfirm(
          context = context,
          error = it.error(),
          message = null
        )
    )
}