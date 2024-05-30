import java.io.File

fun main() {
    val input = File("test.txt").readText()
    val scanner = Lexer(input)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val ast = parser.parse()

    if (ast != null) {
        val geoJson = ast.eval()
        println(geoJson)
    } else {
        println("Parsing failed.")
    }
}
