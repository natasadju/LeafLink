import kotlinx.serialization.json.*

sealed class ASTNode {
    abstract fun eval(context: Context): JsonElement

    data class Program(val city: City) : ASTNode() {
        override fun eval(context: Context): JsonElement {
            return city.eval(context)
        }
    }

    data class City(val name: String, val blocks: List<Block>) : ASTNode() {
        override fun eval(context: Context): JsonElement {
            return buildJsonObject {
                put("type", "FeatureCollection")
                put("features", buildJsonArray {
                    blocks.forEach { add(it.eval(context)) }
                })
            }
        }
    }

    sealed class Block : ASTNode() {
        data class Road(val name: String, val commands: List<Command>) : Block() {
            override fun eval(context: Context): JsonElement {
                return buildJsonObject {
                    put("type", "Feature")
                    put("geometry", buildJsonObject {
                        put("type", "LineString")
                        put("coordinates", buildJsonArray {
                            commands.forEach { add(it.eval(context)) }
                        })
                    })
                    put("properties", buildJsonObject {
                        put("name", name)
                    })
                }
            }
        }
        // Add other block types like Park, Building, etc.
    }

    sealed class Command : ASTNode() {
        data class Let(val name: String, val value: Expression) : Command() {
            override fun eval(context: Context): JsonElement {
                val evaluatedValue = value.eval(context)
                context.variables[name] = evaluatedValue
                return buildJsonObject { }
            }
        }

        data class Bend(val start: Expression, val end: Expression, val angle: Expression) : Command() {
            override fun eval(context: Context): JsonElement {
                val startPoint = start.eval(context).jsonArray
                val endPoint = end.eval(context).jsonArray
                return buildJsonArray {
                    add(startPoint)
                    add(endPoint)
                }
            }
        }

        data class Line(val start: Expression, val end: Expression) : Command() {
            override fun eval(context: Context): JsonElement {
                val startPoint = start.eval(context).jsonArray
                val endPoint = end.eval(context).jsonArray
                return buildJsonArray {
                    add(startPoint)
                    add(endPoint)
                }
            }
        }

        // Add other command types like Curve, Box, etc.
    }

    sealed class Expression : ASTNode() {
        data class Number(val value: Double) : Expression() {
            override fun eval(context: Context): JsonElement {
                return buildJsonObject {
                    put("value", value)
                }
            }
        }

        data class Variable(val name: String) : Expression() {
            override fun eval(context: Context): JsonElement {
                return context.variables[name]
                    ?: throw IllegalArgumentException("Undefined variable: $name")
            }
        }

        data class Binary(val left: Expression, val operator: TokenType, val right: Expression) : Expression() {
            override fun eval(context: Context): JsonElement {
                val leftValue = left.eval(context)["value"]!!.jsonPrimitive.double
                val rightValue = right.eval(context)["value"]!!.jsonPrimitive.double
                val result = when (operator) {
                    TokenType.PLUS -> leftValue + rightValue
                    TokenType.MINUS -> leftValue - rightValue
                    TokenType.MULTIPLY -> leftValue * rightValue
                    TokenType.DIVIDE -> leftValue / rightValue
                    else -> throw IllegalArgumentException("Unsupported operator: $operator")
                }
                return buildJsonObject {
                    put("value", result)
                }
            }
        }

        data class Fst(val point: Expression) : Expression() {
            override fun eval(context: Context): JsonElement {
                val pointArray = point.eval(context)["value"]!!.jsonArray
                return buildJsonObject {
                    put("value", pointArray[0].jsonPrimitive.double)
                }
            }
        }

        data class Snd(val point: Expression) : Expression() {
            override fun eval(context: Context): JsonElement {
                val pointArray = point.eval(context)["value"]!!.jsonArray
                return buildJsonObject {
                    put("value", pointArray[1].jsonPrimitive.double)
                }
            }
        }
    }
}

class Context {
    val variables = mutableMapOf<String, JsonElement>()
}
