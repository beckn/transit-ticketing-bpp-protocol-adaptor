package transit.ticketing.bpp.protocol.protocol.discovery.controllers

import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ResponseMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import transit.ticketing.bpp.protocol.protocol.discovery.services.SearchService
import transit.ticketing.bpp.protocol.protocol.shared.dtos.SearchRequestDto
import transit.ticketing.bpp.protocol.schemas.factories.ContextFactory

@RestController
class SearchController @Autowired constructor(
  val searchService: SearchService,
  val contextFactory: ContextFactory
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/client/v1/search")
  @ResponseBody
  fun searchV1(@RequestBody request: SearchRequestDto): ResponseEntity<ProtocolAckResponse> {
    val protocolContext =
      contextFactory.create(transactionId = request.context.transactionId)
    return searchService.search(protocolContext, request.message.criteria)
      .fold(
        {
          log.error("Error during search. Error: {}", it)
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