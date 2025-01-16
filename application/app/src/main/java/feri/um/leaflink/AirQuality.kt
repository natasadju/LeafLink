package feri.um.leaflink

data class AirQuality(
    val _id: String,
    val station: String,
    val pm10: Double?,
    val pm25: Double?,
    val so2: Double?,
    val co: Double?,
    val ozon: Double?,
    val no2: Double?,
    val benzen: Double?,
    val isFake: Boolean,
    val timestamp: String,
    val __v: Int
)
