import java.io.FileInputStream

fun main(args: Array<String>) {

    val inputFile = "test.txt"
    FileInputStream(inputFile).use { inputStream ->
        val lexer = Lexer(inputStream)
        val parser = Parser(lexer)
        parser.parseProgram()
        println("Parsing completed successfully.")
    }
}
