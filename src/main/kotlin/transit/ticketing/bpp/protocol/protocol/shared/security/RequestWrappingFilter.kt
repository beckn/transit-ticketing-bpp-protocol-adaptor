package transit.ticketing.bpp.protocol.protocol.shared.security

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.util.StreamUtils
import java.io.*
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper


@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestWrappingFilter : Filter {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)
  override fun init(filterConfig: FilterConfig?) {}

  override fun doFilter(request: ServletRequest?, response: ServletResponse?, filterChain: FilterChain) {
    val wrapped = if (request != null && request is HttpServletRequest){
      log.info("Wrapping incoming request")
      CachedBodyHttpServletRequest(request)
    } else request

    filterChain.doFilter(wrapped, response)
  }

  override fun destroy() {}
}


class CachedBodyHttpServletRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
  private val cachedBody: ByteArray = StreamUtils.copyToByteArray(request.inputStream)

  @Throws(IOException::class)
  override fun getInputStream(): ServletInputStream? {
    return CachedBodyServletInputStream(cachedBody)
  }

  override fun getReader(): BufferedReader {
    val byteArrayInputStream = ByteArrayInputStream(cachedBody)
    return BufferedReader(InputStreamReader(byteArrayInputStream))
  }
}

class CachedBodyServletInputStream(cachedBody: ByteArray) : ServletInputStream() {
  private val cachedBodyInputStream: InputStream = ByteArrayInputStream(cachedBody)
  override fun read(): Int {
    return cachedBodyInputStream.read()
  }

  override fun isFinished(): Boolean {
    return cachedBodyInputStream.available() == 0
  }

  override fun isReady(): Boolean = true

  override fun setReadListener(listener: ReadListener?) {}

}