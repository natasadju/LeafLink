import java.io.File
import java.io.FileInputStream

fun main() {
    val input = File("test.txt").readText()
    val scanner = Lexer(input)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val ast = parser.parse()
    println(ast)
//    if (parser.parse()) {
//        println("Parsing succeeded.")
//    } else {
//        println("Parsing failed.")
//    }
}
