package transit.ticketing.bpp.protocol.schemas.factories


import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeoutException

class CircuitBreakerFactory {
  companion object {
    fun create(name: String): CircuitBreaker {
      return CircuitBreaker.of(
        name, CircuitBreakerConfig.custom()
          .failureRateThreshold(50f)
          .slowCallRateThreshold(50f)
          .waitDurationInOpenState(Duration.ofMillis(1000))
          .slowCallDurationThreshold(Duration.ofSeconds(8))
          .permittedNumberOfCallsInHalfOpenState(1)
          .minimumNumberOfCalls(10)
          .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
          .slidingWindowSize(10)
          .recordExceptions(IOException::class.java, TimeoutException::class.java)
          .build()
      )
    }
  }
}