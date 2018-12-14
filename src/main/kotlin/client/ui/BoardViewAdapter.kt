package client.ui

import client.model.GameManager
import common.HexCoord
import javafx.beans.property.ObjectProperty
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import tornadofx.add
import tornadofx.c
import tornadofx.div
import kotlin.math.cos

class BoardViewAdapter(val gameManager: GameManager, val availableColors: List<Color>,val chosenColor: ObjectProperty<Paint>) {
    val fields get() = gameManager.game.board.fields
    val players get() = gameManager.game.players
    val currentNumberOfPlayers get() = gameManager.game.players.count()
    //val expectedNumberOfPlayers
    val corners get() = gameManager.game.corners


    fun getBoard(playerId: Int,onFieldClickedHandler: (node: Node, field: common.SixSidedStarBoard.Field) -> Unit) : Pane {
            gameManager.game.fillBoardCorners(corners)
        for (pl in players) {

        }

        val pane = Pane()
        for ((key, value) in fields) {
            val c = Circle(15.0, c("603BB7"))
            setLocationOfCircle(c,key,pane)
            //c.setOnMouseClicked { event: MouseEvent -> onFieldClickedHandler(c,value); event.consume() }
            pane.add(c)
            value.piece?.let {
                val pawn = Circle(15.0,chosenColor.get())
                setLocationOfCircle(pawn,key,pane)
                pawn.setOnMouseClicked { event: MouseEvent -> onFieldClickedHandler(pawn,value); event.consume() }
                pane.add(pawn)
            }

        }
        return pane
    }

    fun setLocationOfCircle(c: Circle,hexCoord: HexCoord,parent: Pane) {
        c.centerXProperty().bind(parent.widthProperty() / 2)
        c.centerYProperty().bind(parent.heightProperty() / 2)
        c.translateY = -hexCoord.z * 29.0
        c.translateX = hexCoord.x * 17 * cos(60.0) - hexCoord.y * 17 * cos(60.0)
    }
}
