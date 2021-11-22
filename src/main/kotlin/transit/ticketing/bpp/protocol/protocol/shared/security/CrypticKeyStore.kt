package transit.ticketing.bpp.protocol.protocol.shared.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

interface CrypticKeyStore {
  fun getBase64PublicKey(subscriberId: String, keyId: String): String?
}

@Component
@Order(value=2)
class ConfigFileKeyStore @Autowired constructor(
  private val env: Environment
) : CrypticKeyStore {
  override fun getBase64PublicKey(subscriberId: String, keyId: String): String =
    env.getRequiredProperty("subscribers.public_keys.$subscriberId.$keyId")
}

@Component
@Order(value=1)
class RegistryKeyStore @Autowired constructor(
  private val registryService: RegistryService
) : CrypticKeyStore {

  override fun getBase64PublicKey(subscriberId: String, keyId: String): String? =
    registryService.lookupBppById(subscriberId).map { it.first().signing_public_key }.orNull()
}