import java.io.InputStream

enum class TokenType {
    CITY, ROAD, BUILDING, PARK, LAKE, GREENFIELD, BENCH, SCULPTURE, PUBLICSIGN, ISLAND,
    PLAYGROUND, WALKINGTRAIL, BIKELANE, BRIDGE, UNDERGROUNDTUNNEL, LINE, BEND, BOX, CIRC,
    ELLIP, ARC, POLYLINE, POLYSPLINE, CURVE, HEIGHT, MATERIAL, COLOR, TEXT, POINT, REAL,
    STRING, LBRACE, RBRACE, LPAREN, RPAREN, COMMA, DOT, EOF, ERROR
}

data class Token(val type: TokenType, val lexeme: String, val row: Int, val column: Int)

class Lexer(private val input: InputStream) {
    private var row = 1
    private var column = 0
    private var currentChar: Int = input.read()

    private fun read(): Int {
        val temp = currentChar
        currentChar = input.read()
        column++
        if (currentChar == '\n'.code) {
            row++
            column = 0
        }
        return temp
    }

    private fun peek(): Int {
        return currentChar
    }

    private fun isDigit(char: Int): Boolean {
        return char in '0'.code..'9'.code
    }

    private fun isLetter(char: Int): Boolean {
        return char in 'a'.code..'z'.code || char in 'A'.code..'Z'.code
    }

    private fun readWhile(condition: (Int) -> Boolean): String {
        val result = StringBuilder()
        while (condition(peek())) {
            result.append(read().toChar())
        }
        return result.toString()
    }

    fun nextToken(): Token {
        while (currentChar != -1) {
            val i = when (currentChar) {
                ' '.code, '\t'.code, '\n'.code, '\r'.code -> {
                    read()  // Skip whitespace
                }

                '{'.code -> {
                    read()
                    return Token(TokenType.LBRACE, "{", row, column)
                }

                '}'.code -> {
                    read()
                    return Token(TokenType.RBRACE, "}", row, column)
                }

                '('.code -> {
                    read()
                    return Token(TokenType.LPAREN, "(", row, column)
                }

                ')'.code -> {
                    read()
                    return Token(TokenType.RPAREN, ")", row, column)
                }

                ','.code -> {
                    read()
                    return Token(TokenType.COMMA, ",", row, column)
                }

                '.'.code -> {
                    read()
                    return Token(TokenType.DOT, ".", row, column)
                }

                '"'.code -> {
                    read()
                    val str = readWhile { it != '"'.code }
                    read()  // Skip closing "
                    return Token(TokenType.STRING, str, row, column)
                }

                in '0'.code..'9'.code -> {
                    val num = readWhile { it in '0'.code..'9'.code || it == '.'.code }
                    return Token(TokenType.REAL, num, row, column)
                }

                else -> {
                    if (isLetter(currentChar)) {
                        val word = readWhile { isLetter(it) }
                        return when (word) {
                            "City" -> Token(TokenType.CITY, word, row, column)
                            "Road" -> Token(TokenType.ROAD, word, row, column)
                            "Building" -> Token(TokenType.BUILDING, word, row, column)
                            "Park" -> Token(TokenType.PARK, word, row, column)
                            "Lake" -> Token(TokenType.LAKE, word, row, column)
                            "Greenfield" -> Token(TokenType.GREENFIELD, word, row, column)
                            "Bench" -> Token(TokenType.BENCH, word, row, column)
                            "Sculpture" -> Token(TokenType.SCULPTURE, word, row, column)
                            "PublicSign" -> Token(TokenType.PUBLICSIGN, word, row, column)
                            "Island" -> Token(TokenType.ISLAND, word, row, column)
                            "Playground" -> Token(TokenType.PLAYGROUND, word, row, column)
                            "WalkingTrail" -> Token(TokenType.WALKINGTRAIL, word, row, column)
                            "BikeLane" -> Token(TokenType.BIKELANE, word, row, column)
                            "Bridge" -> Token(TokenType.BRIDGE, word, row, column)
                            "UndergroundTunnel" -> Token(TokenType.UNDERGROUNDTUNNEL, word, row, column)
                            "line" -> Token(TokenType.LINE, word, row, column)
                            "bend" -> Token(TokenType.BEND, word, row, column)
                            "box" -> Token(TokenType.BOX, word, row, column)
                            "circ" -> Token(TokenType.CIRC, word, row, column)
                            "ellip" -> Token(TokenType.ELLIP, word, row, column)
                            "arc" -> Token(TokenType.ARC, word, row, column)
                            "polyline" -> Token(TokenType.POLYLINE, word, row, column)
                            "polyspline" -> Token(TokenType.POLYSPLINE, word, row, column)
                            "curve" -> Token(TokenType.CURVE, word, row, column)
                            "height" -> Token(TokenType.HEIGHT, word, row, column)
                            "material" -> Token(TokenType.MATERIAL, word, row, column)
                            "color" -> Token(TokenType.COLOR, word, row, column)
                            "text" -> Token(TokenType.TEXT, word, row, column)
                            else -> Token(TokenType.ERROR, word, row, column)
                        }
                    }
                    read()  // Skip unrecognized character
                }
            }
        }
        return Token(TokenType.EOF, "", row, column)
    }
}
