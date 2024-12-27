enum class TokenType {
    CITY, ROAD, BUILDING, PARK, LAKE, GREENFIELD, BENCHES, SCULPTURES, PUBLIC_SIGN, ISLAND, PLAYGROUND, WALKING_TRAIL,
    LINE, BEND, BOX, CIRC, ELLIP, ARC, POLYLINE, CURVE, POINT, REAL, ANGLE, STRING, LBRACE, RBRACE, LPAREN,
    RPAREN, COMMA, EOF, ERROR, MARKER, LET, EQUALSTO, FOREACH, NEAR, FIND
}

data class Token(val type: TokenType, val lexeme: String, val position: Int)

enum class State {
    START,
    IDENTIFIER,
    NUMBER,
    STRING,
    DONE,
    ERROR
}
class Lexer(private val input: String) {
    private var position = 0
    private val keywords = mapOf(
        "City" to TokenType.CITY,
        "Road" to TokenType.ROAD,
        "Building" to TokenType.BUILDING,
        "Park" to TokenType.PARK,
        "Lake" to TokenType.LAKE,
        "Greenfield" to TokenType.GREENFIELD,
        "Benches" to TokenType.BENCHES,
        "Sculptures" to TokenType.SCULPTURES,
        "PublicSign" to TokenType.PUBLIC_SIGN,
        "Island" to TokenType.ISLAND,
        "Playground" to TokenType.PLAYGROUND,
        "Walkingtrail" to TokenType.WALKING_TRAIL,
        "line" to TokenType.LINE,
        "bend" to TokenType.BEND,
        "box" to TokenType.BOX,
        "circ" to TokenType.CIRC,
        "ellip" to TokenType.ELLIP,
        "arc" to TokenType.ARC,
        "polyline" to TokenType.POLYLINE,
        "curve" to TokenType.CURVE,
        "marker" to TokenType.MARKER,
        "foreach" to TokenType.FOREACH,
        "near" to TokenType.NEAR,
        "find" to TokenType.FIND,
        "let" to TokenType.LET,
        "point" to TokenType.POINT
    )

    private val tokens = mutableListOf<Token>()

    fun scanTokens(): List<Token> {
        var state = State.START
        var start = 0

        while (position < input.length) {
            when (state) {
                State.START -> {
                    when (val ch = input[position]) {
                        '{' -> addToken(TokenType.LBRACE)
                        '}' -> addToken(TokenType.RBRACE)
                        '(' -> addToken(TokenType.LPAREN)
                        ')' -> addToken(TokenType.RPAREN)
                        ',' -> addToken(TokenType.COMMA)
                        '=' -> addToken(TokenType.EQUALSTO)
                        '"' -> {
                            state = State.STRING
                            start = position
                        }
                        in '0'..'9' -> {
                            state = State.NUMBER
                            start = position
                        }
                        in 'a'..'z', in 'A'..'Z' -> {
                            state = State.IDENTIFIER
                            start = position
                        }
                        ' ', '\t', '\r', '\n' -> position++ // Ignore whitespace
                        else -> {
                            addToken(TokenType.ERROR)
                            position++
                        }
                    }
                }
                State.IDENTIFIER -> {
                    while (position < input.length && input[position].isLetter()) {
                        position++
                    }
                    val lexeme = input.substring(start, position)
                    val type = keywords[lexeme] ?: TokenType.ERROR
                    tokens.add(Token(type, lexeme, start))
                    state = State.START
                }
                State.NUMBER -> {
                    while (position < input.length && (input[position].isDigit() || input[position] == '.')) {
                        position++
                    }
                    tokens.add(Token(TokenType.REAL, input.substring(start, position), start))
                    state = State.START
                }
                State.STRING -> {
                    position++ // Skip opening quote
                    while (position < input.length && input[position] != '"') {
                        position++
                    }
                    if (position < input.length) {
                        position++ // Skip closing quote
                    }
                    tokens.add(Token(TokenType.STRING, input.substring(start, position), start))
                    state = State.START
                }
                else -> throw IllegalStateException("Invalid state: $state")
            }
        }

        tokens.add(Token(TokenType.EOF, "", position))
        return tokens
    }

    private fun addToken(type: TokenType) {
        tokens.add(Token(type, input[position].toString(), position))
        position++
    }
}
