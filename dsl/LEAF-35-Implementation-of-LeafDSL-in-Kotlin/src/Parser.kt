class Parser(private val lexer: Lexer) {
    private var currentToken: Token = lexer.nextToken()

    private fun match(expected: TokenType) {
        if (currentToken.type == expected) {
            currentToken = lexer.nextToken()
        } else {
            error("Expected $expected but found ${currentToken.type} at row ${currentToken.row}, column ${currentToken.column}")
        }
    }

    fun parseProgram() {

        parseCity()
    }

    private fun parseCity() {
        match(TokenType.CITY)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        parseBlocks()
        match(TokenType.RBRACE)
    }

    private fun parseBlocks() {
        parseBlock()
        while (currentToken.type in setOf(
                TokenType.ROAD, TokenType.BUILDING, TokenType.PARK, TokenType.LAKE,
                TokenType.GREENFIELD, TokenType.BENCH, TokenType.SCULPTURE, TokenType.PUBLICSIGN,
                TokenType.ISLAND, TokenType.PLAYGROUND, TokenType.WALKINGTRAIL, TokenType.BIKELANE,
                TokenType.BRIDGE, TokenType.UNDERGROUNDTUNNEL
            )) {
            parseBlock()
        }
    }

    private fun parseBlock() {
        when (currentToken.type) {
            TokenType.ROAD -> parseSpecificBlock(TokenType.ROAD)
            TokenType.BUILDING -> parseSpecificBlock(TokenType.BUILDING)
            TokenType.PARK -> parseSpecificBlock(TokenType.PARK)
            TokenType.LAKE -> parseSpecificBlock(TokenType.LAKE)
            TokenType.GREENFIELD -> parseSpecificBlock(TokenType.GREENFIELD)
            TokenType.BENCH -> parsePointBlock(TokenType.BENCH)
            TokenType.SCULPTURE -> parsePointBlock(TokenType.SCULPTURE)
            TokenType.PUBLICSIGN -> parsePointBlock(TokenType.PUBLICSIGN)
            TokenType.ISLAND -> parseSpecificBlock(TokenType.ISLAND)
            TokenType.PLAYGROUND -> parseSpecificBlock(TokenType.PLAYGROUND)
            TokenType.WALKINGTRAIL -> parseWalkingTrailBlock()
            TokenType.BIKELANE -> parseSpecificBlock(TokenType.BIKELANE)
            TokenType.BRIDGE -> parseSpecificBlock(TokenType.BRIDGE)
            TokenType.UNDERGROUNDTUNNEL -> parseSpecificBlock(TokenType.UNDERGROUNDTUNNEL)
            else -> error("Unexpected block type: ${currentToken.type}")
        }
    }

    private fun parseSpecificBlock(type: TokenType) {
        match(type)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        parseCommands()
        match(TokenType.RBRACE)
    }

    private fun parsePointBlock(type: TokenType) {
        match(type)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        parsePoint()
        match(TokenType.RBRACE)
    }

    private fun parseWalkingTrailBlock() {
        match(TokenType.WALKINGTRAIL)
        match(TokenType.STRING)
        match(TokenType.LBRACE)
        parsePoint()
        match(TokenType.COMMA)
        parsePoint()
        match(TokenType.RBRACE)
    }

    private fun parseCommands() {
        parseCommand()
        while (currentToken.type in setOf(
                TokenType.LINE, TokenType.BEND, TokenType.BOX, TokenType.CIRC, TokenType.ELLIP,
                TokenType.ARC, TokenType.POLYLINE, TokenType.POLYSPLINE, TokenType.CURVE,
                TokenType.HEIGHT, TokenType.MATERIAL, TokenType.COLOR, TokenType.TEXT
            )) {
            parseCommand()
        }
    }

    private fun parseCommand() {
        when (currentToken.type) {
            TokenType.LINE -> parseLineCommand()
            TokenType.BEND -> parseBendCommand()
            TokenType.BOX -> parseBoxCommand()
            TokenType.CIRC -> parseCircCommand()
            TokenType.ELLIP -> parseEllipCommand()
            TokenType.ARC -> parseArcCommand()
            TokenType.POLYLINE -> parsePolylineCommand(TokenType.POLYLINE)
            TokenType.POLYSPLINE -> parsePolylineCommand(TokenType.POLYSPLINE)
            TokenType.CURVE -> parseCurveCommand()
            TokenType.HEIGHT -> parseHeightCommand()
            TokenType.MATERIAL -> parseMaterialCommand()
            TokenType.COLOR -> parseColorCommand()
            TokenType.TEXT -> parseTextCommand()
            else -> error("Unexpected command type: ${currentToken.type}")
        }
    }

    private fun parseLineCommand() {
        match(TokenType.LINE)
        match(TokenType.LPAREN)
        parsePoint()
        match(TokenType.COMMA)
        parsePoint()
        match(TokenType.RPAREN)
    }

    private fun parseBendCommand() {
        match(TokenType.BEND)
        match(TokenType.LPAREN)
        parsePoint()
        match(TokenType.COMMA)
        parsePoint()
        match(TokenType.COMMA)
        parseReal()
        match(TokenType.RPAREN)
    }

    private fun parseBoxCommand() {
        match(TokenType.BOX)
        match(TokenType.LPAREN)
        parsePoint()
        match(TokenType.COMMA)
        parsePoint()
        match(TokenType.RPAREN)
    }

    private fun parseCircCommand() {
        match(TokenType.CIRC)
        match(TokenType.LPAREN)
        parsePoint()
        match(TokenType.COMMA)
        parseReal()
        match(TokenType.RPAREN)
    }

    private fun parseEllipCommand() {
        match(TokenType.ELLIP)
        match(TokenType.LPAREN)
        parsePoint()
        match(TokenType.COMMA)
        parseReal()
        match(TokenType.COMMA)
        parseReal()
        match(TokenType.RPAREN)
    }

    private fun parseArcCommand() {
        match(TokenType.ARC)
        match(TokenType.LPAREN)
        parsePoint()
        match(TokenType.COMMA)
        parseReal()
        match(TokenType.COMMA)
        parseReal()
        match(TokenType.RPAREN)
    }

    private fun parsePolylineCommand(type: TokenType) {
        match(type)
        match(TokenType.LPAREN)
        parsePoints()
        match(TokenType.RPAREN)
    }

    private fun parseCurveCommand() {
        match(TokenType.CURVE)
        match(TokenType.LPAREN)
        parsePoint()
        match(TokenType.COMMA)
        parsePoint()
        match(TokenType.COMMA)
        parsePoint()
        match(TokenType.RPAREN)
    }

    private fun parseHeightCommand() {
        match(TokenType.HEIGHT)
        match(TokenType.LPAREN)
        parseReal()
        match(TokenType.RPAREN)
    }

    private fun parseMaterialCommand() {
        match(TokenType.MATERIAL)
        match(TokenType.LPAREN)
        match(TokenType.STRING)
        match(TokenType.RPAREN)
    }

    private fun parseColorCommand() {
        match(TokenType.COLOR)
        match(TokenType.LPAREN)
        match(TokenType.STRING)
        match(TokenType.RPAREN)
    }

    private fun parseTextCommand() {
        match(TokenType.TEXT)
        match(TokenType.LPAREN)
        match(TokenType.STRING)
        match(TokenType.RPAREN)
    }

    private fun parsePoints() {
        parsePoint()
        while (currentToken.type == TokenType.COMMA) {
            match(TokenType.COMMA)
            parsePoint()
        }
    }

    private fun parsePoint() {
        match(TokenType.LPAREN)
        parseReal()
        match(TokenType.COMMA)
        parseReal()
        match(TokenType.RPAREN)
    }

    private fun parseReal() {
        match(TokenType.REAL)
    }
}
