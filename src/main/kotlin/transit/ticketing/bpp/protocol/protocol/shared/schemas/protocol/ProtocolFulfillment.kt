package transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol

data class ProtocolFulfillment @Default constructor(
    val id: String? = null,
    val start: ProtocolFulfillmentStart? = null,
    val end: ProtocolFulfillmentEnd? = null,
)

data class ProtocolCustomer @Default constructor(
    val person: ProtocolPerson? = null,
    val contact: ProtocolContact? = null
)

data class ProtocolState @Default constructor(
    val descriptor: ProtocolDescriptor? = null,
    val updatedAt: java.time.OffsetDateTime? = null,
    val updatedBy: String? = null
)

data class ProtocolPerson @Default constructor(
    val name: String? = null,
    val image: String? = null,
    val dob: java.time.LocalDate? = null,
    val gender: String? = null,
    val cred: String? = null,
    val tags: Map<String, String>? = null
)

data class ProtocolVehicle @Default constructor(
    val category: String? = null,
    val capacity: Int? = null,
    val make: String? = null,
    val model: String? = null,
    val size: String? = null,
    val variant: String? = null,
    val color: String? = null,
    val energyType: String? = null,
    val registration: String? = null
)

// TODO Similar classes
data class ProtocolFulfillmentStart @Default constructor(
    val location: ProtocolLocation? = null,
    val time: ProtocolTime? = null,
    val instructions: ProtocolDescriptor? = null,
    val contact: ProtocolContact? = null
)

// TODO Similar classes
data class ProtocolFulfillmentEnd @Default constructor(
    val location: ProtocolLocation? = null,
    val time: ProtocolTime? = null,
    val instructions: ProtocolDescriptor? = null,
    val contact: ProtocolContact? = null
)


data class ProtocolContact @Default constructor(
    val phone: String? = null,
    val email: String? = null,
    val tags: Map<String, String>? = null
)