package transit.ticketing.bpp.protocol

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SandboxBapApplication

fun main(args: Array<String>) {
  runApplication<SandboxBapApplication>(*args)
}
