sealed class ASTNode {
    data class Program(val city: City) : ASTNode()
    data class City(val name: String, val blocks: List<Block>) : ASTNode()

    sealed class Block : ASTNode() {
        data class Road(val name: String, val commands: List<Command>) : Block()
        data class Building(val name: String, val commands: List<Command>) : Block()
        data class Park(val name: String, val commands: List<Command>) : Block()
        data class Lake(val name: String, val commands: List<Command>) : Block()
        data class Greenfield(val name: String, val commands: List<Command>) : Block()
        data class Benches(val name: String, val point: Point) : Block()
        data class Sculptures(val name: String, val point: Point) : Block()
        data class PublicSign(val name: String, val point: Point) : Block()
        data class Island(val name: String, val commands: List<Command>) : Block()
        data class Playground(val name: String, val commands: List<Command>) : Block()
        data class WalkingTrail(val name: String, val startPoint: Point, val endPoint: Point) : Block()
    }

    sealed class Command : ASTNode() {
        data class Line(val start: Point, val end: Point) : Command()
        data class Bend(val start: Point, val end: Point, val angle: Angle) : Command()
        data class Box(val start: Point, val end: Point) : Command()
        data class Circ(val center: Point, val radius: Real) : Command()
        data class Ellip(val center: Point, val radius1: Real, val radius2: Real) : Command()
        data class Arc(val center: Point, val startAngle: Angle, val endAngle: Angle) : Command()
        data class Polyline(val points: List<Point>) : Command()
        data class Polyspline(val points: List<Point>) : Command()
        data class BendExtended(val start: Point, val end: Point, val angle1: Angle, val angle2: Angle, val real: Real) : Command()
        data class Curve(val points: List<Point>) : Command()
    }

    data class Point(val x: Real, val y: Real) : ASTNode()
    data class Real(val value: Double) : ASTNode()
    data class Angle(val value: Double) : ASTNode()
}
