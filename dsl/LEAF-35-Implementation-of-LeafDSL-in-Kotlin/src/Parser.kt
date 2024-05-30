class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): Boolean {
        try {
            program()
            return tokens[current].type == TokenType.EOF
        } catch (e: ParseException) {
            println(e.message)
            return false
        }
    }

    private fun program() {
        city()
    }

    private fun city() {
        match(TokenType.CITY)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        blocks()
        match(TokenType.RBRACE)
    }

    private fun blocks() {
        while (tokens[current].type in setOf(
                TokenType.ROAD, TokenType.BUILDING, TokenType.PARK, TokenType.LAKE,
                TokenType.GREENFIELD, TokenType.BENCHES, TokenType.SCULPTURES, TokenType.PUBLIC_SIGN,
                TokenType.ISLAND, TokenType.PLAYGROUND, TokenType.WALKING_TRAIL
            )
        ) {
            block()
        }
    }

    private fun block() {
        when (tokens[current].type) {
            TokenType.ROAD -> road()
            TokenType.BUILDING -> building()
            TokenType.PARK -> park()
            TokenType.LAKE -> lake()
            TokenType.GREENFIELD -> greenfield()
            TokenType.BENCHES -> benches()
            TokenType.SCULPTURES -> sculptures()
            TokenType.PUBLIC_SIGN -> publicSign()
            TokenType.ISLAND -> island()
            TokenType.PLAYGROUND -> playground()
            TokenType.WALKING_TRAIL -> walkingTrail()
            else -> throw ParseException("Unexpected token: ${tokens[current].lexeme}")
        }
    }

    private fun road() {
        match(TokenType.ROAD)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        commands()
        match(TokenType.RBRACE)
    }

    private fun building() {
        match(TokenType.BUILDING)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        commands()
        match(TokenType.RBRACE)
    }

    private fun park() {
        match(TokenType.PARK)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        commands()
        match(TokenType.RBRACE)
    }

    private fun lake() {
        match(TokenType.LAKE)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        commands()
        match(TokenType.RBRACE)
    }

    private fun greenfield() {
        match(TokenType.GREENFIELD)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        commands()
        match(TokenType.RBRACE)
    }

    private fun benches() {
        match(TokenType.BENCHES)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        point()
        match(TokenType.RBRACE)
    }

    private fun sculptures() {
        match(TokenType.SCULPTURES)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        point()
        match(TokenType.RBRACE)
    }

    private fun publicSign() {
        match(TokenType.PUBLIC_SIGN)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        point()
        match(TokenType.RBRACE)
    }

    private fun island() {
        match(TokenType.ISLAND)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        commands()
        match(TokenType.RBRACE)
    }

    private fun playground() {
        match(TokenType.PLAYGROUND)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        commands()
        match(TokenType.RBRACE)
    }

    private fun walkingTrail() {
        match(TokenType.WALKING_TRAIL)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        point()
        match(TokenType.COMMA)
        point()
        match(TokenType.RBRACE)
    }

    private fun commands() {
        while (tokens[current].type in setOf(
                TokenType.LINE, TokenType.BEND, TokenType.BOX, TokenType.CIRC, TokenType.ELLIP,
                TokenType.ARC, TokenType.POLYLINE, TokenType.POLYSPLINE, TokenType.CURVE
            )
        ) {
            command()
        }
    }

    private fun command() {
        when (tokens[current].type) {
            TokenType.LINE -> {
                match(TokenType.LINE)
                match(TokenType.LPAREN)
                point()
                match(TokenType.COMMA)
                point()
                match(TokenType.RPAREN)
            }
            TokenType.BEND -> {
                match(TokenType.BEND)
                match(TokenType.LPAREN)
                point()
                match(TokenType.COMMA)
                point()
                match(TokenType.COMMA)
                angle()
                match(TokenType.RPAREN)
            }
            TokenType.BOX -> {
                match(TokenType.BOX)
                match(TokenType.LPAREN)
                point()
                match(TokenType.COMMA)
                point()
                match(TokenType.RPAREN)
            }
            TokenType.CIRC -> {
                match(TokenType.CIRC)
                match(TokenType.LPAREN)
                point()
                match(TokenType.COMMA)
                real()
                match(TokenType.RPAREN)
            }
            TokenType.ELLIP -> {
                match(TokenType.ELLIP)
                match(TokenType.LPAREN)
                point()
                match(TokenType.COMMA)
                real()
                match(TokenType.COMMA)
                real()
                match(TokenType.RPAREN)
            }
            TokenType.ARC -> {
                match(TokenType.ARC)
                match(TokenType.LPAREN)
                point()
                match(TokenType.COMMA)
                angle()
                match(TokenType.COMMA)
                angle()
                match(TokenType.RPAREN)
            }
            TokenType.POLYLINE -> {
                match(TokenType.POLYLINE)
                match(TokenType.LPAREN)
                points()
                match(TokenType.RPAREN)
            }
            TokenType.POLYSPLINE -> {
                match(TokenType.POLYSPLINE)
                match(TokenType.LPAREN)
                points()
                match(TokenType.RPAREN)
            }
            TokenType.CURVE -> {
                match(TokenType.CURVE)
                match(TokenType.LPAREN)
                point()
                match(TokenType.COMMA)
                point()
                match(TokenType.COMMA)
                point()
                match(TokenType.RPAREN)
            }
            else -> throw ParseException("Unexpected token: ${tokens[current].lexeme}")
        }
    }

    private fun points() {
        point()
        while (tokens[current].type == TokenType.COMMA) {
            match(TokenType.COMMA)
            point()
        }
    }

    private fun point() {
        match(TokenType.LPAREN)
        real()
        match(TokenType.COMMA)
        real()
        match(TokenType.RPAREN)
    }

    private fun real() {
        if (tokens[current].type == TokenType.REAL) {
            match(TokenType.REAL)
        } else {
            throw ParseException("Expected real number, found: ${tokens[current].lexeme}")
        }
    }

    private fun angle() {
        real()
    }

    private fun match(type: TokenType) {
        if (tokens[current].type == type) {
            current++
        } else {
            throw ParseException("Expected token ${type}, found: ${tokens[current].lexeme}")
        }
    }

    class ParseException(message: String) : Exception(message)
}
