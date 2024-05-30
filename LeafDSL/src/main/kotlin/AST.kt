import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.atan2
import kotlin.math.sqrt

sealed class ASTNode {
    abstract fun eval(): JsonObject
    data class Program(val city: City) : ASTNode() {
        override fun eval(): JsonObject {
            return city.eval()
        }
    }

    data class City(val name: String, val blocks: List<Block>) : ASTNode() {
        override fun eval(): JsonObject {
            val features = blocks.map { it.eval() }
            val featureCollection = JsonObject(
                mapOf(
                    "type" to JsonPrimitive("FeatureCollection"),
                    "features" to JsonArray(features)
                )
            )
            return featureCollection
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
                TODO("Not yet implemented")
            }
        }

        data class Lake(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                TODO("Not yet implemented")
            }
        }

        data class Greenfield(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                TODO("Not yet implemented")
            }
        }

        data class Benches(val name: String, val point: Point) : Block() {
            override fun eval(): JsonObject {
                TODO("Not yet implemented")
            }
        }

        data class Sculptures(val name: String, val point: Point) : Block() {
            override fun eval(): JsonObject {
                TODO("Not yet implemented")
            }
        }

        data class PublicSign(val name: String, val point: Point) : Block() {
            override fun eval(): JsonObject {
                TODO("Not yet implemented")
            }
        }

        data class Island(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                TODO("Not yet implemented")
            }
        }

        data class Playground(val name: String, val commands: List<Command>) : Block() {
            override fun eval(): JsonObject {
                TODO("Not yet implemented")
            }
        }

        data class WalkingTrail(val name: String, val startPoint: Point, val endPoint: Point) : Block() {
            override fun eval(): JsonObject {
                TODO("Not yet implemented")
            }
        }
    }

    sealed class Command {

        abstract fun eval(): JsonArray
        data class Line(val start: Point, val end: Point) : Command() {
            override fun eval(): JsonArray {
                val coordinates = JsonArray(listOf(start.eval(), end.eval()))

                return coordinates
            }
        }

        data class Bend(val start: Point, val end: Point, val angle: Angle) : Command() {
            override fun eval(): JsonArray {
                TODO("Not yet implemented")
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
                TODO("Not yet implemented")
            }
        }

        data class Ellip(val center: Point, val radius1: Real, val radius2: Real) : Command() {
            override fun eval(): JsonArray {
                TODO("Not yet implemented")
            }
        }

        data class Arc(val center: Point, val startAngle: Angle, val endAngle: Angle) : Command() {
            override fun eval(): JsonArray {
                TODO("Not yet implemented")
            }
        }

        data class Polyline(val points: List<Point>) : Command() {
            override fun eval(): JsonArray {
                TODO("Not yet implemented")
            }
        }

        data class Polyspline(val points: List<Point>) : Command() {
            override fun eval(): JsonArray {
                TODO("Not yet implemented")
            }
        }

        data class BendExtended(val start: Point, val end: Point, val angle1: Angle, val angle2: Angle, val real: Real) : Command() {
            override fun eval(): JsonArray {
                TODO("Not yet implemented")
            }
        }

        data class Curve(val points: List<Point>) : Command() {
            override fun eval(): JsonArray {
                TODO("Not yet implemented")
            }
        }
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
            TODO("Not yet implemented")
        }
    }
}
