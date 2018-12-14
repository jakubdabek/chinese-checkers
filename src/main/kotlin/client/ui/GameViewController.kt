package client.ui

import client.model.GameManager
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import tornadofx.*

class GameViewController : Controller() {
    private lateinit var gameManager: GameManager
    private lateinit var boardViewAdapter: BoardViewAdapter
    private val view: AppGameView by inject()
    val availableColors =
        listOf<Color>(Color.RED, Color.GREEN, Color.YELLOW, Color.DARKVIOLET, Color.ORANGE, Color.DARKBLUE)
    val chosenColorProperty: SimpleObjectProperty<Paint> = SimpleObjectProperty(Color.DARKSLATEGREY)
    val chosenColor by chosenColorProperty


    internal fun initGameManager(gameManager: GameManager) {
        chosenColorProperty.set(availableColors[0])
        gameManager.game.corners[0] = 0
        gameManager.game.corners[1] = 3
        this.gameManager = gameManager
    }

    fun getBoard(): Pane {
        boardViewAdapter = BoardViewAdapter(gameManager, availableColors, chosenColorProperty)
        val pane = boardViewAdapter.getBoard(gameManager.playerId)
        pane.prefWidthProperty().bind(view.root.widthProperty())
        pane.prefHeightProperty().bind(view.root.heightProperty())
        return pane
    }

    fun endTurn() {

    }

    fun pass() {

    }

    fun exitGame() {
        //send exit message
        view.replaceWith<AppMenuView>()
    }
}