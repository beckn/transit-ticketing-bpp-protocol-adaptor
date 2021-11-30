package transit.ticketing.bpp.protocol.protocol.shared.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.StreamUtils
import org.springframework.web.servlet.HandlerInterceptor
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ResponseMessage
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SignatureVerificationInterceptor @Autowired constructor(
  private val keyStores: List<CrypticKeyStore>,
  private val authHeaders: List<String>,
  private val objectMapper: ObjectMapper
) : HandlerInterceptor {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    val authHeaderName = getAuthHeaderName(request)
    val authorization = Authorization.parse(request.getHeader(authHeaderName))
    val isValid = verify(request, authorization) ?: false
    if (!isValid) {
      log.error("Signature verification failed for header: $authHeaderName")
      sendErrorResponse(response, authorization, authHeaderName)
    }
    return isValid
  }

  private fun getAuthHeaderName(request: HttpServletRequest) =
    authHeaders.firstOrNull { request.getHeader(it) != null } ?: authHeaders.first()


  private fun verify(request: HttpServletRequest, authorization: Authorization?): Boolean? {
   if(authorization != null) {
     return authorization?.let {
       val b64PublicKey = getBase64PublicKey(it) ?: return false
       val requestBytes = StreamUtils.copyToByteArray(request.inputStream)
//       return it.isNotExpired()  && Cryptic.verify(it, b64PublicKey, String(requestBytes))
       return  Cryptic.verify(it, b64PublicKey, String(requestBytes))
     }
   }else{
     return false
   }
  }

  private fun getBase64PublicKey(authorization: Authorization): String? {
    val (subscriberId, uniqueKeyId, _) = authorization.parseKey
    return keyStores.map { it.getBase64PublicKey(subscriberId, uniqueKeyId) }.find { it != null }
  }

  private fun sendErrorResponse(
    response: HttpServletResponse,
    authorization: Authorization?,
    authHeaderName: String?
  ) {
    val (subscriberId, _, _) = authorization?.parseKey ?: Triple("", "", "")
    response.status = 401
    response.setHeader(
      failureHeaderName(authHeaderName),
      """Signature realm=$subscriberId,headers=${authorization?.headers}"""
    )
    response.contentType = "application/json"
    val err = ProtocolAckResponse(message = ResponseMessage.nack(), context = null)
    response.writer.write(objectMapper.writeValueAsString(err))
  }

  private fun failureHeaderName(authHeaderName: String?): String {
    val name = authHeaderName ?: return "WWW-Authenticate"
    return if(name.contains("Proxy")) "Proxy-Authenticate" else "WWW-Authenticate"
  }

}
