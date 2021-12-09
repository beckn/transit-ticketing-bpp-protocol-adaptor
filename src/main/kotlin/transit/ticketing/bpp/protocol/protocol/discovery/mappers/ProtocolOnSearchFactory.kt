package transit.ticketing.bpp.protocol.protocol.discovery.mappers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.Availability
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.Location
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.SearchResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.protocol.shared.security.Cryptic


@Component
class ProtocolOnSearchFactory @Autowired constructor() {
    val arrival = "Arrival"
    val departure = "Departure"
    val providerName = "State Water Transport Department"
    val providerIdCode = "KSWTD"

    fun create(response: SearchResponse? = null, context: ProtocolContext, intent: ProtocolIntent? = null) =
        ProtocolOnSearch(
            context = context,
            message = ProtocolOnSearchMessage(
                catalog = ProtocolCatalog(
                    bppDescriptor = ProtocolDescriptor(name = providerName, code = providerIdCode),
                    bppProviders = listOf(
                        ProtocolProviderCatalog(
                            id = providerIdCode,
                            descriptor = ProtocolDescriptor(name = providerName),
                            fulfillments = getFullfillmentList(response?.availability),
                            locations = getStopsLocation(response?.location),
                            items = getItemsFromClient(response?.availability)
                        )
                    )
                )
            )
        )

    private fun getItemsFromClient(availabilityList: List<Availability>?): List<ProtocolItem>? {
        val itemList = ArrayList<ProtocolItem>()
        if (!availabilityList.isNullOrEmpty()) {
            for (availability in availabilityList) {
                itemList.add(
                    ProtocolItem(
                        id = availability.trip_id.toString(),
                        fulfillmentId = availability.trip_id.toString(),
                        descriptor = ProtocolDescriptor(
                            name = ""
                        ),
                        price = ProtocolPrice(
                            currency = availability.fare.currency,
                            value = availability.fare.amount.toString()
                        ),
                        quantity = ProtocolItemQuantityAllocated(count = availability.seats)
                    )
                )

            }
        }
        return itemList
    }

    private fun getStopsLocation(location: List<Location>?): List<ProtocolLocation>? {
        val listLocations = ArrayList<ProtocolLocation>()
        if (!location.isNullOrEmpty()) {
            for (loc: Location in location) {
                listLocations.add(
                    ProtocolLocation(
                        id = loc.stopId,
                        descriptor = ProtocolDescriptor(
                            name = loc.stopName
                        ),
                        gps = loc.gps,
                        stationCode = loc.stopId
                    )
                )
            }
        }
        return listLocations
    }

    private fun getFullfillmentList(availability: List<Availability>?): List<ProtocolFulfillment>? {
        val listOfFullfillment = ArrayList<ProtocolFulfillment>()
        if (!availability.isNullOrEmpty()) {
            for (avail: Availability in availability) {
                listOfFullfillment.add(
                    ProtocolFulfillment(
                        id = generateFullFillmentId(avail),
                        start = ProtocolFulfillmentStart(
                            location = ProtocolLocation(
                                id = avail.departure.stopId,
                            ),
                            time = ProtocolTime(
                                label = departure,
                                timestamp = avail.departure.timestamp
                            )
                        ),
                        end = ProtocolFulfillmentEnd(
                            location = ProtocolLocation(
                                id = avail.arrival.stopId,
                            ),
                            time = ProtocolTime(
                                label = arrival,
                                timestamp = avail.arrival.timestamp
                            )
                        )
                    )
                )
            }
        }
        return listOfFullfillment
    }

    private fun generateFullFillmentId(avail: Availability): String {
        val data = avail.trip_id.toString()+","+avail.arrival+","+avail.departure
        val uuid =  Util.uuidToBase64(data)
        return uuid
    }
}