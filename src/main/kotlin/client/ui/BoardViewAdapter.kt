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
import javafx.scene.shape.LineTo
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
    var chosenMove: HexMove? = null
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
//        println(chosenColor)
//        println(corners)
//        println(cornersAndColors)
    }

    private data class Pawn(var position: HexCoord, val circle: Circle, val color: Color)
    private val pawns = mutableListOf<Pawn>()
    fun getBoard(): Pane {
        val pane = Pane()
        val circles = mutableMapOf<HexCoord, Circle>()
        pawns.clear()
        for ((position, field) in fields) {
            val c = Circle(15.0)
            c.addClass(Styles.unselectedField)
            setLocationOfCircle(c, position, pane)
            //c.setOnMouseClicked { event: MouseEvent -> onFieldClickedHandler(c,field); event.consume() }
            c.setOnMouseClicked { println(position.toString()) }
            circles[position] = c
            pane.add(c)
            field.piece?.let {
                val color = cornersAndColors.getValue(it.cornerId)
                val pawnCircle = Circle(15.0, color)
                pawnCircle.viewOrder = -1.0
                val pawn = Pawn(position, pawnCircle, color)
                setLocationOfCircle(pawnCircle, position, pane)
                pane.add(pawnCircle)
                pawns.add(pawn)
                if (it.cornerId == corners.getValue(gameManager.playerId))
                    pawnCircle.setOnMouseClicked { event: MouseEvent -> pawnClickedHandler(pawn, field); event.consume() }
            }
        }
        fieldCircles = circles
        pane.setOnMouseClicked { emptyClickedHandler() }
        pane.isDisable = true
        return pane
    }

    private fun setLocationOfCircle(c: Circle, hexCoord: HexCoord, parent: Pane) {
        c.layoutXProperty().bind(parent.widthProperty() / 2)
        c.layoutYProperty().bind(parent.heightProperty() / 2)
        c.translateY = -0.5 * (hexCoord.x + hexCoord.y) * 54
        c.translateX = -hexCoord.x * 17 * cos(60.0) + hexCoord.y * 17 * cos(60.0)
    }

    private fun emptyClickedHandler() {
        println("empty clicked handler called")
        chosenPawn?.let { (_, circle, _) -> circle.removeClass(Styles.selectedField) }
        chosenPawn = null
        chosenMove = null
        for (c in highlightedCircles) {
            c.removeClass(Styles.highlightedField)
            c.removeClass(Styles.chosenAsDestination)
            c.addClass(Styles.unselectedField)
            c.setOnMouseClicked { emptyClickedHandler(); it.consume() }
        }
        highlightedCircles.clear()
    }

    private fun pawnClickedHandler(pawn: Pawn, field: common.SixSidedStarBoard.Field) {
        emptyClickedHandler()
        chosenPawn = null
        chosenMove = null
        field.piece?.let {
            chosenPawn = pawn
            pawn.circle.addClass(Styles.selectedField)
            gameManager.requestAvailableMoves(pawn.position)
        }
    }

    fun redrawBoard() {

    }

    fun highlightCircle(c: Circle) {
        c.addClass(Styles.highlightedField)
    }

    fun highlightPossibleMoves() {
        gameManager.possibleMoves?.map { fieldCircles.getValue(it.destination) to it }?.forEach { (circle, move) ->
            highlightedCircles.add(circle)
            highlightCircle(circle)

            circle.setOnMouseClicked { event ->
                println("highlighted clicked")
                chosenMove = move
                for (highlightedCircle in highlightedCircles) {
                    highlightedCircle.removeClass(Styles.chosenAsDestination)
                                     //.addClass(Styles.highlightedField)
                }
                circle.addClass(Styles.chosenAsDestination)
                event.consume()
            }
        }
    }

    fun performMove(move: HexMove) {
        val path = Path()
        val movedPawn = pawns.first { it.position == move.origin }
        path.elements.add(MoveTo(movedPawn.circle.translateX, movedPawn.circle.translateY))
        path.elements.addAll(move.movements.map {
            LineTo(fieldCircles.getValue(it.second).translateX, fieldCircles.getValue(it.second).translateY)
        })
        val pathTransition = PathTransition(Duration(400.0 * path.elements.size), path, movedPawn.circle)
        movedPawn.position = move.destination
        pathTransition.play()
        emptyClickedHandler()
    }
}
