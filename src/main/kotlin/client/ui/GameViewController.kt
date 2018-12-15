package client.ui

import client.model.CommunicationManager
import client.model.GameManager
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import tornadofx.*

class GameViewController : Controller() {
    private lateinit var gameManager: GameManager
    private lateinit var boardViewAdapter: BoardViewAdapter
    private lateinit var client: CommunicationManager
    private val view: AppGameView by inject()
    val availableColors =
        listOf<Color>(Color.RED, Color.GREEN, Color.YELLOW, Color.DARKVIOLET, Color.ORANGE, Color.DARKBLUE)
    val chosenColorProperty: SimpleObjectProperty<Paint> = SimpleObjectProperty(Color.DARKSLATEGREY)
    val chosenColor by chosenColorProperty


    internal fun initClientAndGameManager(client: CommunicationManager, gameManager: GameManager) {
        chosenColorProperty.set(availableColors[0])
        gameManager.setMessageProducedHandler(client::sendMessageToServer)
        gameManager.setGameEventHandler(this::handleGameEvent)
        gameManager.game.corners[0] = 0
        gameManager.game.corners[1] = 3
        this.gameManager = gameManager

    }

    private fun handleGameEvent(event: GameManager.Event) {
        when (event) {

            GameManager.Event.TurnStarted -> TODO()
            GameManager.Event.AvailableMovesChanged -> TODO()
            GameManager.Event.GameEndedInterrupted -> TODO()
            GameManager.Event.GameEndedConcluded -> TODO()
        }
    }

    fun getBoard(): Pane {
        boardViewAdapter = BoardViewAdapter(gameManager, availableColors, chosenColorProperty)
        //gameManager.setMessageProducedHandler(boardViewAdapter::redrawBoard)
        val pane = boardViewAdapter.getBoard(gameManager.playerId)
        pane.prefWidthProperty().bind(view.root.widthProperty())
        pane.prefHeightProperty().bind(view.root.heightProperty())
        return pane
    }

    fun endTurn() {
        gameManager.endTurn(boardViewAdapter.chosenFieldCoords)
    }

    fun pass() {
        gameManager.pass()
    }

    fun exitGame() {
        gameManager.exitGame()
        view.replaceWith<AppMenuView>()
    }
}