import kotlinx.serialization.decodeFromString
import java.io.File
import kotlinx.serialization.json.*

fun main() {
    val input = File("test2.txt").readText()
    val scanner = Lexer(input)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val ast = parser.parse()

    if (ast != null) {
        val geoJsonn = ast.eval()
        println(geoJsonn)
        val input = File("output.txt").readText()
        val geoJson = Json.decodeFromString<GeoJson>(input)
        val polygons = geoJson.features.map { it.geometry }

        polygons.forEachIndexed { index, polygon ->
            val coordinates = polygon.coordinates
            if (hasSelfIntersections(coordinates)) {
                println("Polygon $index has self-intersections.")
            }
        }

        for (i in polygons.indices) {
            for (j in i + 1 until polygons.size) {
                if (hasIntersections(polygons[i].coordinates, polygons[j].coordinates)) {
                    println("Polygon $i intersects with Polygon $j.")
                }
            }
        }

    } else {
        println("Parsing failed.")
    }


}

//marker "u" = (15.646477734844638,
//          46.56900135289388)
//foreach "x" near "u" { find "x" }