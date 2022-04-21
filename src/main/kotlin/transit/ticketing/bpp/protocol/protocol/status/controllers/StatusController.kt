package transit.ticketing.bpp.protocol.protocol.status.controllers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.shared.dtos.OrderStatusRequestDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.protocol.status.services.StatusService

@RestController
class StatusController @Autowired constructor(
  val statusService: StatusService
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/protocol/v1/status")
  @ResponseBody
  fun statusV1(@RequestBody request: OrderStatusRequestDto): ResponseEntity<ProtocolOnOrderStatus> {

    return statusService.getOrderStatusRequest(request.context)
      .fold(
        {
          log.error("Error during init request. Error: {}", it)
          mapToErrorResponse(it, request.context)
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
      ProtocolOnOrderStatus(
        context = context,
        error = it.error(),
        message = null
      )
    )

  @GetMapping("/protocol/v1/payment")
  @ResponseBody
  fun paymentV1(@RequestParam transactionId: String): ResponseEntity<ProtocolOnOrderStatus> {
    return if(!transactionId.isNullOrEmpty()){
      statusService.getPaymentDetails(transactionId)
        .fold(
          {
            log.error("Error during init request. Error: {}", it)
            mapToErrorResponse(it)
          },
          {
            log.info("Successfully initiated Search")
            ResponseEntity.ok(it)
          }
        )
    }else {
      mapToErrorResponse(BppError.BadRequestError)
    }
  }
}