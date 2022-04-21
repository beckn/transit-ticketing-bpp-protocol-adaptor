package transit.ticketing.bpp.protocol.protocol.shared

import java.nio.ByteBuffer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


/** This class is to define all common functions or business logic which can be reused in the project */
object Util {
  /** Validate BaseUrl ends with slash or not
   *@param baseUrl String
   * @return baseUrl String
   **/
  fun getBaseUri(baseUrl: String): String {
    return if (baseUrl.endsWith("/", true)) baseUrl else "$baseUrl/"
  }

  fun uuidToBase64(str: String): String {

    val base64 = Base64.getEncoder()
    val uuid = UUID.fromString(str)
    val bb = ByteBuffer.wrap(ByteArray(16))
    bb.putLong(uuid.mostSignificantBits)
    bb.putLong(uuid.leastSignificantBits)
    return base64.encodeToString(bb.array())
  }

  fun uuidFromBase64(key: String): String? {
    val base64 = Base64.getDecoder()
    val bytes: ByteArray = base64.decode(key)
    val bb: ByteBuffer = ByteBuffer.wrap(bytes)
    val uuid = UUID(bb.getLong(), bb.getLong())
    return uuid.toString()
  }

  fun dateToMiliseconds(dateString: String): String? {
      val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      var testDate: Date? = null
      try {
        testDate = sdf.parse(dateString)
        return testDate!!.time.toString()
      } catch (e: ParseException) {
        // TODO Auto-generated catch block
        e.printStackTrace()
        return null
      }
  }

  fun miliSecondsToDateString(dateString: String): String? {
    val noDataFound = ""
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    return try {
      val milliSeconds: Long = dateString.toLong()
      val today = Calendar.getInstance()

      val calendar = Calendar.getInstance()
      calendar.timeInMillis = milliSeconds
      if(!isBeforeDay(calendar,today)){
        sdf.format(calendar.time)
      }else{
        noDataFound
      }
    } catch (e: ParseException) {
      // TODO Auto-generated catch block
      e.printStackTrace()
      noDataFound
    }
  }

  fun isBeforeDay(cal1: Calendar?, cal2: Calendar?): Boolean {
    require(!(cal1 == null || cal2 == null)) { "The dates must not be null" }
    if (cal1[Calendar.ERA] < cal2[Calendar.ERA]) return true
    if (cal1[Calendar.ERA] > cal2[Calendar.ERA]) return false
    if (cal1[Calendar.YEAR] < cal2[Calendar.YEAR]) return true
    return if (cal1[Calendar.YEAR] > cal2[Calendar.YEAR]) false else cal1[Calendar.DAY_OF_YEAR] < cal2[Calendar.DAY_OF_YEAR]
  }

  fun formatYYYYmmDD(dateString: String): String? {
    val outputFormat = SimpleDateFormat("yyyy-MM-dd")
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    return try {
      val dateInput = inputFormat.parse(dateString)
      outputFormat.format(dateInput)
    } catch (e: ParseException) {
      // TODO Auto-generated catch block
      e.printStackTrace()
      null
    }
  }

  fun formatHHmm(dateString: String): String? {
    val outputFormat = SimpleDateFormat("HH:mm")
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    return try {
      val dateInput = inputFormat.parse(dateString)
      outputFormat.format(dateInput)
    } catch (e: ParseException) {
      // TODO Auto-generated catch block
      e.printStackTrace()
      null
    }
  }

  fun getCurrentDateInString(): String {
    return try {
      val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      inputFormat.format(Date())
    } catch (e:Exception) {
      ""
    }
  }
}