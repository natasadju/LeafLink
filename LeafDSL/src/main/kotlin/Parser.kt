class Parser(private val tokens: List<Token>) {
    private var current = 0
    var failed = false

    fun parse(): ASTNode.Program? {
        return try {
            program()
        } catch (e: ParseException) {
            println("Parsing failed: ${e.message}")
            null
        }
    }

    private fun program(): ASTNode.Program {
        val city = city()
        return ASTNode.Program(city)
    }

    private fun city(): ASTNode.City {
        match(TokenType.CITY)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val blocks = blocks()
        match(TokenType.RBRACE)
        return ASTNode.City(name, blocks)
    }

    private fun blocks(): List<ASTNode.Block> {
        val blocks = mutableListOf<ASTNode.Block>()
        do {
            blocks.add(block())
        } while (tokens[current].type in listOf(
                TokenType.ROAD, TokenType.BUILDING, TokenType.PARK,
                TokenType.LAKE, TokenType.GREENFIELD, TokenType.BENCHES,
                TokenType.SCULPTURES, TokenType.PUBLIC_SIGN, TokenType.ISLAND,
                TokenType.PLAYGROUND, TokenType.WALKING_TRAIL))
        return blocks
    }

    private fun block(): ASTNode.Block {
        return when (tokens[current].type) {
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
            TokenType.WALKING_TRAIL -> walkingtrail()
            else -> throw ParseException("Unexpected token: ${tokens[current].lexeme}")
        }
    }

    private fun road(): ASTNode.Block.Road {
        match(TokenType.ROAD)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val commands = commands()
        match(TokenType.RBRACE)
        return ASTNode.Block.Road(name, commands)
    }

    private fun building(): ASTNode.Block.Building {
        match(TokenType.BUILDING)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val commands = commands()
        match(TokenType.RBRACE)
        return ASTNode.Block.Building(name, commands)
    }

    private fun park(): ASTNode.Block.Park {
        match(TokenType.PARK)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val commands = commands()
        match(TokenType.RBRACE)
        return ASTNode.Block.Park(name, commands)
    }

    private fun lake(): ASTNode.Block.Lake {
        match(TokenType.LAKE)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val commands = commands()
        match(TokenType.RBRACE)
        return ASTNode.Block.Lake(name, commands)
    }

    private fun greenfield(): ASTNode.Block.Greenfield {
        match(TokenType.GREENFIELD)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val commands = commands()
        match(TokenType.RBRACE)
        return ASTNode.Block.Greenfield(name, commands)
    }

    private fun benches(): ASTNode.Block.Benches {
        match(TokenType.BENCHES)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val point = point()
        match(TokenType.RBRACE)
        return ASTNode.Block.Benches(name, point)
    }

    private fun sculptures(): ASTNode.Block.Sculptures {
        match(TokenType.SCULPTURES)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val point = point()
        match(TokenType.RBRACE)
        return ASTNode.Block.Sculptures(name, point)
    }

    private fun publicSign(): ASTNode.Block.PublicSign {
        match(TokenType.PUBLIC_SIGN)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val point = point()
        match(TokenType.RBRACE)
        return ASTNode.Block.PublicSign(name, point)
    }

    private fun island(): ASTNode.Block.Island {
        match(TokenType.ISLAND)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val commands = commands()
        match(TokenType.RBRACE)
        return ASTNode.Block.Island(name, commands)
    }

    private fun playground(): ASTNode.Block.Playground {
        match(TokenType.PLAYGROUND)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val commands = commands()
        match(TokenType.RBRACE)
        return ASTNode.Block.Playground(name, commands)
    }

    private fun walkingtrail(): ASTNode.Block.WalkingTrail {
        match(TokenType.WALKING_TRAIL)
        val name = match(TokenType.STRING).lexeme
        match(TokenType.LBRACE)
        val startPoint = point()
        match(TokenType.COMMA)
        val endPoint = point()
        match(TokenType.RBRACE)
        return ASTNode.Block.WalkingTrail(name, startPoint, endPoint)
    }

    private fun commands(): List<ASTNode.Command> {
        val commands = mutableListOf<ASTNode.Command>()
        do {
            commands.add(command())
        } while (tokens[current].type in listOf(
                TokenType.LINE, TokenType.BEND, TokenType.BOX,
                TokenType.CIRC, TokenType.ELLIP, TokenType.ARC,
                TokenType.POLYLINE, TokenType.POLYSPLINE, TokenType.CURVE))
        return commands
    }

    private fun command(): ASTNode.Command {
        return when (tokens[current].type) {
            TokenType.LINE -> {
                match(TokenType.LINE)
                match(TokenType.LPAREN)
                val start = point()
                match(TokenType.COMMA)
                val end = point()
                match(TokenType.RPAREN)
                ASTNode.Command.Line(start, end)
            }
            TokenType.BEND -> {
                match(TokenType.BEND)
                match(TokenType.LPAREN)
                val start = point()
                match(TokenType.COMMA)
                val end = point()
                match(TokenType.COMMA)
                val angle1 = angle()
                if (tokens[current].type == TokenType.COMMA) {
                    match(TokenType.COMMA)
                    val angle2 = angle()
                    if (tokens[current].type == TokenType.COMMA) {
                        match(TokenType.COMMA)
                        val real = real()
                        match(TokenType.RPAREN)
                        ASTNode.Command.BendExtended(start, end, angle1, angle2, real)
                    } else {
                        match(TokenType.RPAREN)
                        ASTNode.Command.BendExtended(start, end, angle1, angle2, ASTNode.Real(0.0))
                    }
                } else {
                    match(TokenType.RPAREN)
                    ASTNode.Command.Bend(start, end, angle1)
                }
            }
            TokenType.BOX -> {
                match(TokenType.BOX)
                match(TokenType.LPAREN)
                val start = point()
                match(TokenType.COMMA)
                val end = point()
                match(TokenType.RPAREN)
                ASTNode.Command.Box(start, end)
            }
            TokenType.CIRC -> {
                match(TokenType.CIRC)
                match(TokenType.LPAREN)
                val center = point()
                match(TokenType.COMMA)
                val radius = real()
                match(TokenType.RPAREN)
                ASTNode.Command.Circ(center, radius)
            }
            TokenType.ELLIP -> {
                match(TokenType.ELLIP)
                match(TokenType.LPAREN)
                val center = point()
                match(TokenType.COMMA)
                val radius1 = real()
                match(TokenType.COMMA)
                val radius2 = real()
                match(TokenType.RPAREN)
                ASTNode.Command.Ellip(center, radius1, radius2)
            }
            TokenType.ARC -> {
                match(TokenType.ARC)
                match(TokenType.LPAREN)
                val center = point()
                match(TokenType.COMMA)
                val startAngle = angle()
                match(TokenType.COMMA)
                val endAngle = angle()
                match(TokenType.RPAREN)
                ASTNode.Command.Arc(center, startAngle, endAngle)
            }
            TokenType.POLYLINE -> {
                match(TokenType.POLYLINE)
                match(TokenType.LPAREN)
                val points = points()
                match(TokenType.RPAREN)
                ASTNode.Command.Polyline(points)
            }
            TokenType.POLYSPLINE -> {
                match(TokenType.POLYSPLINE)
                match(TokenType.LPAREN)
                val points = points()
                match(TokenType.RPAREN)
                ASTNode.Command.Polyspline(points)
            }
            TokenType.CURVE -> {
                match(TokenType.CURVE)
                match(TokenType.LPAREN)
                val points = points()
                match(TokenType.RPAREN)
                ASTNode.Command.Curve(points)
            }
            else -> throw ParseException("Unexpected token: ${tokens[current].lexeme}")
        }
    }

    private fun points(): List<ASTNode.Point> {
        val points = mutableListOf<ASTNode.Point>()
        do {
            points.add(point())
        } while (tokens[current].type == TokenType.COMMA && match(TokenType.COMMA) != null)
        return points
    }

    private fun point(): ASTNode.Point {
        match(TokenType.LPAREN)
        val x = real()
        match(TokenType.COMMA)
        val y = real()
        match(TokenType.RPAREN)
        return ASTNode.Point(x, y)
    }

    private fun real(): ASTNode.Real {
        val value = match(TokenType.REAL).lexeme.toDouble()
        return ASTNode.Real(value)
    }

    private fun angle(): ASTNode.Angle {
        val value = match(TokenType.REAL).lexeme.toDouble()
        return ASTNode.Angle(value)
    }

    private fun match(type: TokenType): Token {
        if (check(type)) return advance()
        throw ParseException("Expected ${type.name}, but found ${tokens[current].type.name}")
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return tokens[current].type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return tokens[current - 1]
    }

    private fun isAtEnd(): Boolean {
        return current >= tokens.size
    }

    class ParseException(message: String) : Exception(message)
}
