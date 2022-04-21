package transit.ticketing.bpp.protocol.message.entities


import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext



interface BecknResponseDao {
  val context: ProtocolContext?
  val error: ErrorDao?
}

