package client.ui

import client.model.GameManager
import common.HexCoord
import common.HexMove
import common.Player
import javafx.animation.PathTransition
import javafx.beans.property.ObjectProperty
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.util.Duration
import tornadofx.*
import kotlin.math.cos


class BoardViewAdapter(
    val gameManager: GameManager,
    val availableColors: List<Color>,
    val chosenColor: ObjectProperty<Paint>
) {
    private val fields get() = gameManager.game.board.fields
    private lateinit var fieldCircles: Map<HexCoord,Circle>
    private val players get() = gameManager.game.players
    val currentNumberOfPlayers get() = gameManager.game.players.count()
    private val corners get() = gameManager.game.corners
    private var chosenPawn: Pawn? = null
    var chosenFieldCoords: HexCoord? = null
    val highlightedCircles = mutableListOf<Circle>()
    val cornersAndColors = mutableMapOf<Int, Color>()

    init {
        gameManager.game.fillBoardCorners(corners)
        cornersAndColors.put(corners[gameManager.playerId]!!, chosenColor.get() as Color)
        for ((color, key) in
        availableColors.filter { it != chosenColor.get() }
                zip corners.filter { it.key != gameManager.playerId }.values
        ) {
            cornersAndColors[key] = color

        }
        println(chosenColor)
        println(corners)
        println(cornersAndColors)
    }

    private data class Pawn(var position: HexCoord, val circle: Circle, val color: Color)
    private val pawns = mutableListOf<Pawn>()
    fun getBoard(playerId: Player.Id): Pane {
        val pane = Pane()
        val circles = mutableMapOf<HexCoord, Circle>()
        pawns.clear()
        for ((position, field) in fields) {
            val c = Circle(15.0, c("603BB7"))
            setLocationOfCircle(c, position, pane)
            //c.setOnMouseClicked { event: MouseEvent -> onFieldClickedHandler(c,field); event.consume() }
            c.setOnMouseClicked { println(position.toString()) }
            circles[position] = c
            pane.add(c)
            field.piece?.let {
                val color = cornersAndColors.getValue(it.cornerId)
                val pawnCircle = Circle(15.0, color)
                val pawn = Pawn(position, pawnCircle, color)
                setLocationOfCircle(pawnCircle, position, pane)
                pane.add(pawnCircle)
                pawns.add(pawn)
                pawnCircle.setOnMouseClicked { event: MouseEvent -> pawnClickedHandler(pawn, field); event.consume() }
            }
        }
        fieldCircles = circles
        pane.setOnMouseClicked { emptyClickedHandler() }
        return pane
    }

    private fun setLocationOfCircle(c: Circle, hexCoord: HexCoord, parent: Pane) {
        c.layoutXProperty().bind(parent.widthProperty() / 2)
        c.layoutYProperty().bind(parent.heightProperty() / 2)
        c.translateY = -0.5 * (hexCoord.x + hexCoord.y) * 54
        c.translateX = -hexCoord.x * 17 * cos(60.0) + hexCoord.y * 17 * cos(60.0)
    }

    private fun emptyClickedHandler() {
        chosenPawn?.let { (_, circle, color) -> circle.style { circle.fill = color } }
        for (c in highlightedCircles) {
            c.style { c.fill = c("603BB7") }
        }
    }

    private fun pawnClickedHandler(pawn: Pawn, field: common.SixSidedStarBoard.Field) {
        emptyClickedHandler()
        chosenPawn = null
        chosenFieldCoords = null
        field.piece?.let {
            chosenPawn = pawn
            pawn.circle.style(append = true) {
                strokeWidth = 5.px
                stroke = c("black")
                fill = (pawn.circle.fill as Color).deriveColor(0.0, 1.0, 1.5, 1.0)
            }
            gameManager.requestAvailableMoves(pawn.position)
        } ?: run {
            //TODO: make move or not, add HexMove
        }
        println(field.toString())
    }

    fun redrawBoard() {

    }

    fun highlightCircle(c: Circle) {
        c.style(append = true) {
            strokeWidth = 5.px
            stroke = c("gray")
        }
    }

    fun highlightPossibleMoves() {
        gameManager.possibleMoves?.map { fieldCircles.getValue(it.destination) to it }?.forEach { (circle, move) ->
            highlightedCircles.add(circle)
            highlightCircle(circle)
            circle.setOnMouseClicked { gameManager.requestMove(move) }
        }
    }

    fun performMove(move: HexMove) {
//        val path = Path()
//        path.elements.addAll(move.movements.map {
//            MoveTo(fieldCircles.getValue(it.second).translateX, fieldCircles.getValue(it.second).translateY)
//        })
        val movedPawn = pawns.first { it.position == move.origin }
//        val pathTransition = PathTransition(Duration(500.0), path, movedPawn.circle)
        movedPawn.position = move.destination
        movedPawn.circle.translateX = fieldCircles.getValue(move.destination).translateX
        movedPawn.circle.translateY = fieldCircles.getValue(move.destination).translateY
//        pathTransition.play()
        emptyClickedHandler()
    }
}
