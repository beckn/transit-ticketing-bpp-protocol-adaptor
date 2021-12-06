package transit.ticketing.bpp.protocol.message.entities

import com.fasterxml.jackson.annotation.JsonProperty
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default

data class ErrorDao @Default constructor(

  val type: Type,
  val code: String,
  val path: String? = null,
  val message: String? = null
) {
  enum class Type(val value: kotlin.String) {
    @JsonProperty("CONTEXT-ERROR") CONTEXTERROR("CONTEXT-ERROR"),
    @JsonProperty("CORE-ERROR") COREERROR("CORE-ERROR"),
    @JsonProperty("DOMAIN-ERROR") DOMAINERROR("DOMAIN-ERROR"),
    @JsonProperty("POLICY-ERROR") POLICYERROR("POLICY-ERROR"),
    @JsonProperty("JSON-SCHEMA-ERROR") JSONSCHEMAERROR("JSON-SCHEMA-ERROR");
  }
}