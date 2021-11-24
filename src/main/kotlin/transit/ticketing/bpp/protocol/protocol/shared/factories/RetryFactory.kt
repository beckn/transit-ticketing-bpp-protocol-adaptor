package transit.ticketing.bpp.protocol.protocol.shared.factories

import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import org.springframework.boot.web.server.WebServerException
import org.springframework.http.HttpStatus
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException

class RetryFactory {
  companion object {
    fun create(name: String, maxAttempts: Int, initialIntervalInMillis: Long, intervalMultiplier: Double) =
      Retry.of(name,
        RetryConfig.custom<Response<String>>()
          .maxAttempts(maxAttempts)
          .intervalFunction(
            IntervalFunction
              .ofExponentialBackoff(initialIntervalInMillis, intervalMultiplier)
          )
          .retryOnResult { response -> response.code() == HttpStatus.INTERNAL_SERVER_ERROR.value() }
          .retryExceptions(IOException::class.java, TimeoutException::class.java, WebServerException::class.java)
          .failAfterMaxAttempts(true)
          .build())
  }
}