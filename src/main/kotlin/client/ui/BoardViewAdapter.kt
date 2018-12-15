package client.ui

import client.model.GameManager
import common.HexCoord
import common.Player
import javafx.beans.property.ObjectProperty
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import tornadofx.*
import kotlin.math.cos


class BoardViewAdapter(
    val gameManager: GameManager,
    val availableColors: List<Color>,
    val chosenColor: ObjectProperty<Paint>
) {
    private val fields get() = gameManager.game.board.fields
    private val players get() = gameManager.game.players
    val currentNumberOfPlayers get() = gameManager.game.players.count()
    //val expectedNumberOfPlayers
    private val corners get() = gameManager.game.corners
    var chosenField: Pair<Circle, Color>? = null
    var chosenFieldCoords: HexCoord? = null
    val highLightedCircles = mutableListOf<Circle>()
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
        for ((key, value) in fields) {
            val c = Circle(15.0, c("603BB7"))
            setLocationOfCircle(c, key, pane)
            //c.setOnMouseClicked { event: MouseEvent -> onFieldClickedHandler(c,value); event.consume() }
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
        c.translateY = -hexCoord.z * 29.0
        c.translateX = hexCoord.x * 17 * cos(60.0) - hexCoord.y * 17 * cos(60.0)
    }

    private fun emptyClickedHandler() {
//        val pane = event.target as Pane
//        pane.children.filterIsInstance<Circle>().forEach { c ->
//            c.style {
//                fill = c("603BB7")
//            }
//        }
        chosenField?.let { (circle, color) -> circle.style { circle.fill = color } }
        for (c in highLightedCircles) {
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
                gameManager.requestMoves(coords)
            } ?: run {
                //TODO: make move or not, add HexMove
            }
            println(field.toString())
        }
    }
    fun redrawBoard() {

    }
}
