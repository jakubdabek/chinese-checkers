package client.ui

import client.model.GameManager
import common.HexCoord
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.shape.Circle
import javafx.scene.transform.Rotate
import tornadofx.add
import tornadofx.c
import tornadofx.div
import kotlin.math.cos

class BoardViewAdapter(val gameManager: GameManager) {
    val fields get() = gameManager.game.board.fields
    val players get() = gameManager.game.players
    val currentNumberOfPlayers get() = gameManager.game.players.count()
    //val expectedNumberOfPlayers
    val corners get() = gameManager.game.corners

    fun getBoard(playerId: Int,onFieldClickedHandler: (node: Node, hexCoord: HexCoord) -> Unit) : Pane {
        val pane = Pane()
        for ((key, value) in fields) {
            val c = Circle(15.0, c("603BB7"))
            c.centerXProperty().bind(pane.widthProperty() / 2)
            c.centerYProperty().bind(pane.heightProperty() / 2)
            c.translateY = -key.z * 33.0
            c.translateX = key.x * 20 * cos(60.0) - key.y * 20 * cos(60.0)

            c.setOnMouseClicked { event: MouseEvent -> onFieldClickedHandler(c,key); event.consume() }
            value.piece?.let {
                c.fill
            }
            pane.add(c)

        }
        return pane
    }
}
