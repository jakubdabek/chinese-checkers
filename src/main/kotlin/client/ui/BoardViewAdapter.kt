package client.ui

import client.model.GameManager
import common.HexCoord
import common.HexMove
import common.Player
import javafx.animation.PathTransition
import javafx.beans.property.ObjectProperty
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.PathElement
import javafx.util.Duration
import tornadofx.*
import kotlin.math.cos


class BoardViewAdapter(
    val gameManager: GameManager,
    val availableColors: List<Color>,
    val chosenColor: ObjectProperty<Paint>
) {
    private val fields get() = gameManager.game.board.fields
    private val fieldCircles = mutableMapOf<HexCoord,Circle>()
    private val players get() = gameManager.game.players
    val currentNumberOfPlayers get() = gameManager.game.players.count()
    private val corners get() = gameManager.game.corners
    var chosenField: Pair<Circle, Color>? = null
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

    fun getBoard(playerId: Player.Id): Pane {
        val pane = Pane()
        fieldCircles.clear()
        for ((key, value) in fields) {
            val c = Circle(15.0, c("603BB7"))
            setLocationOfCircle(c, key, pane)
            //c.setOnMouseClicked { event: MouseEvent -> onFieldClickedHandler(c,value); event.consume() }
            c.setOnMouseClicked { event: MouseEvent -> println(key.toString()) }
            fieldCircles[key] = c
            pane.add(c)
            value.piece?.let {
                val pawn = Circle(15.0, cornersAndColors[it.cornerId])
                setLocationOfCircle(pawn, key, pane)
                pawn.setOnMouseClicked { event: MouseEvent -> fieldClickedHandler(pawn, value,key); event.consume() }
                pane.add(pawn)
            }

        }
        pane.setOnMouseClicked { emptyClickedHandler() }
        return pane
    }

    private fun setLocationOfCircle(c: Circle, hexCoord: HexCoord, parent: Pane) {
        c.centerXProperty().bind(parent.widthProperty() / 2)
        c.centerYProperty().bind(parent.heightProperty() / 2)
        c.translateY = -0.5* (hexCoord.x + hexCoord.y) * 54
        c.translateX = -hexCoord.x * 17 * cos(60.0) + hexCoord.y * 17 * cos(60.0)
    }

    private fun emptyClickedHandler() {
        chosenField?.let { (circle, color) -> circle.style { circle.fill = color } }
        for (c in highlightedCircles) {
            c.style { c.fill = c("603BB7") }
        }
    }

    private fun fieldClickedHandler(node: Node, field: common.SixSidedStarBoard.Field,coords: HexCoord) {
        if (node is Circle) {
            emptyClickedHandler()
            chosenField = null
            chosenFieldCoords = null
            field.piece?.let {
                chosenField = Pair(node, node.fill as Color)
                chosenFieldCoords = coords
                node.style(append = true) {
                    strokeWidth = 5.px
                    stroke = c("black")
                    fill = (node.fill as Color).deriveColor(0.0, 1.0, 1.5, 1.0)
                }
                gameManager.requestAvailableMoves(coords)
            } ?: run {
                //TODO: make move or not, add HexMove
            }
            println(field.toString())
        }
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
        gameManager.possibleMoves?.map { fieldCircles.getValue(it.destination) to it }?.forEach {
            highlightedCircles.add(it.first)
            highlightCircle(it.first)
            it.first.setOnMouseClicked { event -> gameManager.requestMove(it.second) }
        }
    }

    fun performMove(move: HexMove) {
        val path = Path()
        path.elements.addAll(move.movements.map {
            MoveTo(fieldCircles.getValue(it.second).centerX, fieldCircles.getValue(it.second).centerY)
        })
        val pathTransition = PathTransition(Duration(500.0),path,chosenField!!.first)
        pathTransition.play()
        emptyClickedHandler()
    }
}
