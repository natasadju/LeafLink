import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

@Serializable
data class GeoJson(
    val type: String,
    val features: List<Feature>
)

@Serializable
data class Feature(
    val type: String,
    val properties: Map<String, String>,
    val geometry: Geometry
)

@Serializable
data class Geometry(
    val type: String,
    val coordinates: JsonArray
)

fun hasSelfIntersections(coordinates: JsonArray): Boolean {
    val points = coordinates[0].jsonArray.map { it.jsonArray } // Assuming the first element is the polygon's outer ring
    val n = points.size
    for (i in 0 until n) {
        val p1 = points[i]
        val p2 = points[(i + 1) % n]
        for (j in i + 1 until n) {
            val p3 = points[j]
            val p4 = points[(j + 1) % n]
            if (doIntersect(p1, p2, p3, p4)) {
                return true
            }
        }
    }
    return false
}

fun hasIntersections(coordinates1: JsonArray, coordinates2: JsonArray): Boolean {
    val points1 = coordinates1[0].jsonArray.map { it.jsonArray } // Assuming the first element is the polygon's outer ring
    val points2 = coordinates2[0].jsonArray.map { it.jsonArray }

    for (i in points1.indices) {
        val p1 = points1[i]
        val p2 = points1[(i + 1) % points1.size]
        for (j in points2.indices) {
            val p3 = points2[j]
            val p4 = points2[(j + 1) % points2.size]
            if (doIntersect(p1, p2, p3, p4)) {
                return true
            }
        }
    }
    return false
}

fun doIntersect(p1: JsonArray, p2: JsonArray, p3: JsonArray, p4: JsonArray): Boolean {
    val o1 = orientation(p1, p2, p3)
    val o2 = orientation(p1, p2, p4)
    val o3 = orientation(p3, p4, p1)
    val o4 = orientation(p3, p4, p2)

    if (o1 != o2 && o3 != o4) return true

    if (o1 == 0 && onSegment(p1, p3, p2)) return true
    if (o2 == 0 && onSegment(p1, p4, p2)) return true
    if (o3 == 0 && onSegment(p3, p1, p4)) return true
    if (o4 == 0 && onSegment(p3, p2, p4)) return true

    return false
}

fun orientation(p: JsonArray, q: JsonArray, r: JsonArray): Int {
    val val1 = (q[1].double - p[1].double) * (r[0].double - q[0].double) -
            (q[0].double - p[0].double) * (r[1].double - q[1].double)
    return when {
        val1 > 0 -> 1
        val1 < 0 -> 2
        else -> 0
    }
}

fun onSegment(p: JsonArray, q: JsonArray, r: JsonArray): Boolean {
    return q[0].double <= max(p[0].double, r[0].double) && q[0].double >= min(p[0].double, r[0].double) &&
            q[1].double <= max(p[1].double, r[1].double) && q[1].double >= min(p[1].double, r[1].double)
}

private val JsonElement.double: Double
    get() = this.jsonPrimitive.doubleOrNull ?: throw IllegalStateException("Cannot convert JSON element to Double")

private val JsonPrimitive.doubleOrNull: Double?
    get() = try {
        this.content.toDouble()
    } catch (e: NumberFormatException) {
        null
    }

fun max(a: Double, b: Double): Double = if (a > b) a else b

fun min(a: Double, b: Double): Double = if (a < b) a else b
