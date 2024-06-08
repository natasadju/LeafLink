import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.*

abstract class ASTNode {
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

    

}