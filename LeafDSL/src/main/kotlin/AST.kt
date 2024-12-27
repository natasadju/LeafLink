import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.*
import kotlin.math.*
import java.io.IOException
import okhttp3.*
import kotlinx.serialization.json.Json
import java.util.concurrent.CountDownLatch

@Serializable
data class Park(
    val _id: String,
    val name: String,
    val parkId: String,
    val __v: Int,
    val lat: String,
    val long: String
)

val client = OkHttpClient()
val json = Json { isLenient = true }
fun fetchParks(onResult: (List<Park>?) -> Unit) {
    val request = Request.Builder()
        .url("http://172.211.85.100:3000/parks")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { body ->
                val jsonObject = json.parseToJsonElement(body).jsonObject
                val parksArray = jsonObject["parks"]?.jsonArray
                val parks: List<Park>? = parksArray?.let {
                    json.decodeFromJsonElement<List<Park>>(it)
                }
                onResult(parks)
            } ?: run {
                onResult(null)
            }
        }
    })
}

abstract class ASTNode {
    abstract fun eval(): JsonObject
    data class Program(val city: City) : ASTNode() {
        override fun eval(): JsonObject {
            return city.eval()
        }
    }

    data class City(val name: String, val blocks: List<Block>) : ASTNode() {
        override fun eval(): JsonObject {
            val featuresArray = buildJsonArray {
                blocks.forEach { block ->
                    val feature = block.eval()
                    if (isFeatureCollection(feature)) {
                        // If the feature is a FeatureCollection, nest it in the geometry
                        return feature
                    } else {
                        // Otherwise, add the feature directly
                        add(feature)
                    }
                }
            }

            return JsonObject(
                mapOf(
                    "type" to JsonPrimitive("FeatureCollection"),
                    "features" to featuresArray
                )
            )
        }

        private fun isFeatureCollection(feature: JsonObject): Boolean {
            return feature["type"]?.jsonPrimitive?.content == "FeatureCollection"
        }
    }

    sealed class Block : ASTNode() {

        data class Road(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                val coordinates = JsonArray(commands.flatMap { it.eval() })
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("Feature"),
                        "properties" to properties,
                        "geometry" to JsonObject(
                            mapOf(
                                "coordinates" to coordinates,
                                "type" to JsonPrimitive("LineString")
                            )
                        )
                    )
                )
            }
        }

        data class Building(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                val coordinates = JsonArray(commands.map { it.eval() })
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("Feature"),
                        "properties" to properties,
                        "geometry" to JsonObject(
                            mapOf(
                                "type" to JsonPrimitive("Polygon"),
                                "coordinates" to coordinates
                            )
                        )
                    )
                )
            }
        }

        data class Park(val name: String, val commands: List<Command>) : Block() {
                override fun eval(): JsonObject {
                    val coordinates = JsonArray(commands.map { it.eval() })
                    val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                    return JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("Feature"),
                            "properties" to properties,
                            "geometry" to JsonObject(
                                mapOf(
                                    "type" to JsonPrimitive("Polygon"),
                                    "coordinates" to coordinates
                                )
                            )
                        )
                    )
                }
        }

        data class Lake(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                val coordinates = JsonArray(commands.map { it.eval() })
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("Feature"),
                        "properties" to properties,
                        "geometry" to JsonObject(
                            mapOf(
                                "coordinates" to coordinates,
                                "type" to JsonPrimitive("Polygon")
                            )
                        )
                    )
                )
            }
        }

        data class Greenfield(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                val coordinates = JsonArray(commands.map { it.eval() })
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("Feature"),
                        "properties" to properties,
                        "geometry" to JsonObject(
                            mapOf(
                                "coordinates" to coordinates,
                                "type" to JsonPrimitive("Polygon")
                            )
                        )
                    )
                )
            }
        }

        data class Benches(val name: String, val point: Command.Point) : Block() {
            override fun eval(): JsonObject {
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("Feature"),
                        "properties" to properties,
                        "geometry" to JsonObject(
                            mapOf(
                                "coordinates" to JsonArray((point.eval())),
                                "type" to JsonPrimitive("Point")
                            )
                        )
                    )
                )
            }
        }

        data class Sculptures(val name: String, val point: Command.Point) : Block() {
            override fun eval(): JsonObject {
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("Feature"),
                        "properties" to properties,
                        "geometry" to JsonObject(
                            mapOf(
                                "coordinates" to JsonArray((point.eval())),
                                "type" to JsonPrimitive("Point")
                            )
                        )
                    )
                )
            }
        }

        data class Markers(val name: String, val point: Command.Point) : Block() {
            override fun eval(): JsonObject {
                val coordinates = Command.FindNearMarkers(point) // Get the coordinates
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))
                val featuresArray = buildJsonArray { // Use buildJsonArray to create the array
                    coordinates.eval().forEach { coord ->
                        val pointObject = JsonObject(
                            mapOf(
                                "type" to JsonPrimitive("Feature"),
                                "properties" to properties,
                                "geometry" to JsonObject(
                                    mapOf(
                                        "coordinates" to coord,
                                        "type" to JsonPrimitive("Point")
                                    )
                                )
                            )
                        )
                        add(pointObject)
                    }
                }

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("FeatureCollection"),
                        "features" to featuresArray,

                    )
                )
            }
        }



        data class PublicSign(val name: String, val point: Command.Point) : Block() {
            override fun eval(): JsonObject {
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("Feature"),
                        "properties" to properties,
                        "geometry" to JsonObject(
                            mapOf(
                                "coordinates" to JsonArray((point.eval())),
                                "type" to JsonPrimitive("Point")
                            )
                        )
                    )
                )
            }
        }

        data class Island(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                val coordinates = JsonArray(commands.map { it.eval() })
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("Feature"),
                        "properties" to properties,
                        "geometry" to JsonObject(
                            mapOf(
                                "coordinates" to coordinates,
                                "type" to JsonPrimitive("Polygon")
                            )
                        )
                    )
                )
            }
        }

        data class Playground(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                val coordinates = JsonArray(commands.map { it.eval() })
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("Feature"),
                        "properties" to properties,
                        "geometry" to JsonObject(
                            mapOf(
                                "coordinates" to coordinates,
                                "type" to JsonPrimitive("Polygon")
                            )
                        )
                    )
                )
            }
        }

        data class WalkingTrail(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                val coordinates = JsonArray(commands.flatMap { it.eval() })
                val properties = JsonObject(mapOf("name" to JsonPrimitive(name)))

                return JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("Feature"),
                        "properties" to properties,
                        "geometry" to JsonObject(
                            mapOf(
                                "coordinates" to coordinates,
                                "type" to JsonPrimitive("LineString")
                            )
                        )
                    )
                )
            }
        }
    }

    sealed class Command {

        data class Coordinates(val x: Double, val y: Double) {
            operator fun times(scale: Double) = Coordinates(x * scale, y * scale)
            operator fun plus(other: Coordinates) = Coordinates(x + other.x, y + other.y)
            fun dist(other: Coordinates) = sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
            fun angle(other: Coordinates) = atan2(other.y - y, other.x - x)
            fun offset(dist: Double, angle: Double) = Coordinates(x + dist * cos(angle), y + dist * sin(angle))
        }

        fun Point.toCoordinates() = Coordinates(x, y)
        fun Coordinates.toPoint() = Point(x, y)

        abstract fun eval(): JsonArray
        data class Line(val start: Point, val end: Point) : Command() {
            override fun eval(): JsonArray {
                val coordinates = JsonArray(listOf(start.eval(), end.eval()))

                return coordinates
            }
        }

        data class Bend(val start: Point, val end: Point, val angle: Angle) : Command() {
            override fun eval(): JsonArray {
                val bezier = Bezier.bend(start.toCoordinates(), end.toCoordinates(), angle.value)

                val points = bezier.toPoints(20) // Adjust segmentsCount as needed

                val coordinates = JsonArray(points.map { it.toPoint().eval() })

                return coordinates
            }
        }


        data class Box(val start: Point, val end: Point) : Command() {
            override fun eval(): JsonArray {
                return JsonArray(
                    listOf(
                        start.eval(),
                        Point(start.x, end.y).eval(),
                        end.eval(),
                        Point(end.x, start.y).eval(),
                        start.eval()
                    )
                )
            }
        }

        data class Circ(val center: Point, val radius: Real) : Command() {
            override fun eval(): JsonArray {
                val numSegments = 64 // Number of segments to approximate the circle
                val angleStep = 2 * Math.PI / numSegments

                val points = mutableListOf<Point>()
                for (i in 0 until numSegments) {
                    val angle = i * angleStep
                    val x = center.x + radius.value * Math.cos(angle)
                    val y = center.y + radius.value * Math.sin(angle)
                    //val distanceFromCenter = Math.sqrt((x - center.x) * (x - center.x) + (y - center.y) * (y - center.y))
                    //println("Angle: $angle, X: $x, Y: $y, Distance from center: $distanceFromCenter") // Debugging print statement
                    points.add(Point(x, y))
                }
                // Closing the circle by adding the first point at the end
                points.add(points[0])

                val coordinates = JsonArray(points.map { it.eval() })
                return coordinates
            }
        }

        data class Ellip(val center: Point, val radius1: Real, val radius2: Real) : Command() {
            override fun eval(): JsonArray {
                val numSegments = 36
                val angleStep = 2 * Math.PI / numSegments

                val points = mutableListOf<Point>()
                for (i in 0 until numSegments) {
                    val angle = i * angleStep
                    val x = center.x + radius1.value * Math.cos(angle)
                    val y = center.y + radius2.value * Math.sin(angle)
                    points.add(Point(x, y))
                }
                points.add(points[0])

                val coordinates = JsonArray(points.map { it.eval() })
                return coordinates
            }
        }


        data class Arc(val center: Point, val startAngle: Angle, val endAngle: Angle) : Command() {
            override fun eval(): JsonArray {
                val numSegments = 36
                val angleStep = (endAngle.value - startAngle.value) / numSegments

                val points = mutableListOf<Point>()
                for (i in 0..numSegments) {
                    val angle = startAngle.value + i * angleStep
                    val x = center.x + center.distanceTo(center) * Math.cos(angle)
                    val y = center.y + center.distanceTo(center) * Math.sin(angle)
                    points.add(Point(x, y))
                }

                val coordinates = JsonArray(points.map { it.eval() })
                return coordinates
            }
        }


        data class Polyline(val points: List<Point>) : Command() {
            override fun eval(): JsonArray {
                val coordinates = JsonArray(points.map { it.eval() })
                return coordinates
            }
        }

        data class BendExtended(val start: Point, val end: Point, val angle1: Angle, val angle2: Angle, val real: Real) : Command() {
            override fun eval(): JsonArray {
                val bezier1 = Bezier.bend(start.toCoordinates(), end.toCoordinates(), angle1.value)
                val bezier2 = Bezier.bend(start.toCoordinates(), end.toCoordinates(), angle2.value)
                val points1 = bezier1.toPoints(20)
                val points2 = bezier2.toPoints(20)
                val points = points1 + points2
                val coordinates = JsonArray(points.map { it.toPoint().eval() })
                return coordinates
            }
        }

        data class Curve(val points: List<Point>) : Command() {
            override fun eval(): JsonArray {
                val coordinates = generateCatmullRomSpline(points, 100).map { it.eval() }
                return JsonArray(coordinates)
            }

            private fun generateCatmullRomSpline(points: List<Point>, segments: Int): List<Point> {
                if (points.size < 2) return points

                val splinePoints = mutableListOf<Point>()
                for (i in 0 until points.size - 1) {
                    val p0 = if (i == 0) points[i] else points[i - 1]
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val p3 = if (i == points.size - 2) points[i + 1] else points[i + 2]

                    for (t in 0..segments) {
                        val tNorm = t / segments.toDouble()
                        val x = 0.5 * ((2 * p1.x) + (-p0.x + p2.x) * tNorm + (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * tNorm.pow(2) + (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * tNorm.pow(3))
                        val y = 0.5 * ((2 * p1.y) + (-p0.y + p2.y) * tNorm + (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * tNorm.pow(2) + (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * tNorm.pow(3))
                        splinePoints.add(Point(x, y))
                    }
                }

                return splinePoints
            }
        }

        data class FindNearMarkers(val middlePoint: Point) : Command() {
            override fun eval(): JsonArray {
                val points = mutableListOf<Point>()
                fetchParksWithSync { parks ->
                    if (parks != null) {
                        parks.forEach {park -> //tuka gi dobivame usvari parkovite
                            if (middlePoint.distanceTo2(Point(park.long.toDouble(),park.lat.toDouble())) < 700) {
                                println(park._id)
                                val parkAsPoint = Point(park.long.toDouble(), park.lat.toDouble())
                                points.add(parkAsPoint)
                            }
                        }
                    }
                }
                val coordinates = JsonArray(points.map { it.eval() })
                return coordinates
            }

        }

        fun fetchParksWithSync(onResult: (List<Park>?) -> Unit) {
            val latch = CountDownLatch(1)
            fetchParks { parks ->
                onResult(parks)
                latch.countDown()
            }
            latch.await()
        }

        data class Point(val x: Double, val y: Double) {
            fun eval(): JsonArray {
                return JsonArray(listOf(JsonPrimitive(x), JsonPrimitive(y)))
            }

            fun distanceTo(other: Point): Double {
                val dx = other.x - x
                val dy = other.y - y
                return sqrt(dx * dx + dy * dy)
            }

            fun distanceTo2(other: Point): Double {
                val R = 6371000.0 // Radius of the Earth in meters x = lat y = lon
                val latDistance = Math.toRadians(other.y - y)
                val lonDistance = Math.toRadians(other.x - x)
                val a = sin(latDistance / 2) * sin(latDistance / 2) +
                        cos(Math.toRadians(y)) * cos(Math.toRadians(other.y)) *
                        sin(lonDistance / 2) * sin(lonDistance / 2)
                val c = 2 * atan2(sqrt(a), sqrt(1 - a))
                return R * c
            }

            fun angleTo(other: Point): Double {
                return atan2(other.y - y, other.x - x)
            }
        }

        data class Real(val value: Double) : ASTNode() {
            override fun eval(): JsonObject {
                return JsonObject(mapOf("value" to JsonPrimitive(value)))
            }
        }

        data class Angle(val value: Double) : ASTNode() {
            override fun eval(): JsonObject {
                return JsonObject(mapOf("angle" to JsonPrimitive(value)))
            }
        }
        class Bezier(private val p0: Coordinates, private val p1: Coordinates, private val p2: Coordinates, private val p3: Coordinates) {

            fun at(t: Double) =
                p0 * (1.0 - t).pow(3.0) + p1 * 3.0 * (1.0 - t).pow(2.0) * t + p2 * 3.0 * (1.0 - t) * t.pow(2.0) + p3 * t.pow(3.0)

            fun toPoints(segmentsCount: Int): List<Coordinates> {
                val ps = mutableListOf<Coordinates>()
                for (i in 0 .. segmentsCount) {
                    val t = i / segmentsCount.toDouble()
                    ps.add(at(t))
                }
                return ps
            }

            fun approxLength(): Double {
                val midpoint = at(0.5)
                return p0.dist(midpoint) + midpoint.dist(p3)
            }

            fun resolutionToSegmentsCount(resolution: Double) =
                (resolution * approxLength()).coerceAtLeast(2.0).toInt()

            companion object {
                fun bend(t0: Coordinates, t1: Coordinates, relativeAngle: Double): Bezier {
                    val relativeAngle = Math.toRadians(relativeAngle)
                    val oppositeRelativeAngle = PI - relativeAngle

                    val angle = t0.angle(t1)
                    val dist = t0.dist(t1)
                    val constant = (4 / 3) * tan(PI / 8)

                    val c0 = t0.offset(constant * dist, angle + relativeAngle)
                    val c1 = t1.offset(constant * dist, angle + oppositeRelativeAngle)

                    return Bezier(t0, c0, c1, t1)
                }
            }
        }
    }
    fun findIntersections(polygons: List<JsonObject>) {
        val intersectionPoints = mutableListOf<JsonArray>()

        polygons.forEachIndexed { index, polygon ->
            val coordinates = polygon["geometry"]?.jsonObject?.get("coordinates")?.jsonArray
                ?: return@forEachIndexed
            if (hasSelfIntersections(coordinates)) {
                println("Polygon $index has self-intersections.")
                return
            }
        }

        for (i in polygons.indices) {
            for (j in i + 1 until polygons.size) {
                if (hasIntersections(polygons[i], polygons[j])) {
                    println("Polygon $i intersects with Polygon $j.")
                }
            }
        }

        if (intersectionPoints.isNotEmpty()) {
            println("Found intersections at points: $intersectionPoints")
        } else {
            println("No intersections found.")
        }
    }


    //Converting the JsonArray coordinates into a list of points.
    //Iterating over each edge of the polygon and checks if it intersects with any other edge using the doIntersect function.
    //Returns true if any intersection is found.
    fun hasSelfIntersections(coordinates: JsonArray): Boolean {
        val points = coordinates.map { it.jsonArray }
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

    //Iterates over each edge of the first polygon and checks
    //if it intersects with any edge of the second polygon using the doIntersect function.
    fun hasIntersections(polygon1: JsonObject, polygon2: JsonObject): Boolean {
        val coordinates1 = polygon1["geometry"]!!.jsonObject["coordinates"]!!.jsonArray
        val coordinates2 = polygon2["geometry"]!!.jsonObject["coordinates"]!!.jsonArray
        val points1 = coordinates1.map { it.jsonArray }
        val points2 = coordinates2.map { it.jsonArray }

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

    //Check the orientation of the triplets of points to determine if two line segments intersect.
    //Handle collinear cases where points lie on the same line segment.
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

    //Determine the orientation of the ordered triplet of points (p, q, r). The function returns:
    //0 if points are collinear.
    //1 if the triplet is oriented clockwise.
    //2 if the triplet is oriented counterclockwise.
    fun orientation(p: JsonArray, q: JsonArray, r: JsonArray): Int {
        val val1 = (q[1].double - p[1].double) * (r[0].double - q[0].double) -
                (q[0].double - p[0].double) * (r[1].double - q[1].double)
        return when {
            val1 > 0 -> 1
            val1 < 0 -> 2
            else -> 0
        }
    }

    //Check if point q lies on the line segment pr.
    fun onSegment(p: JsonArray, q: JsonArray, r: JsonArray): Boolean {
        return q[0].double <= max(p[0].double, r[0].double) && q[0].double >= min(p[0].double, r[0].double) &&
                q[1].double <= max(p[1].double, r[1].double) && q[1].double >= min(p[1].double, r[1].double)
    }

    private val JsonElement.double: Double
        get() = this.jsonPrimitive.double

    fun max(a: Double, b: Double): Double = if (a > b) a else b

    fun min(a: Double, b: Double): Double = if (a < b) a else b

}