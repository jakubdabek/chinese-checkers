package client.ui

import client.model.CommunicationManager
import client.model.GameManager
import common.HexMove
import common.chinesecheckers.ChineseCheckerServerMessage
import common.chinesecheckers.ChineseCheckersGameMessage
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import tornadofx.*

class GameViewController : Controller() {
    private lateinit var gameManager: GameManager
    private lateinit var boardViewAdapter: BoardViewAdapter
    private var allowBots: Boolean? = null
    internal lateinit var client: CommunicationManager
    private val view: AppGameView by inject()
    val availableColors =
        listOf<Color>(Color.RED, Color.GREEN, Color.YELLOW, Color.DARKVIOLET, Color.ORANGE, Color.DARKBLUE)
    val chosenColorProperty: SimpleObjectProperty<Paint> = SimpleObjectProperty(Color.DARKSLATEGREY)
    private lateinit var numberOfPlayersChosen: List<Int>


    internal fun initClientAndList(
        client: CommunicationManager,
        gameManager: GameManager,
        list: List<Int>,
        allowBots: Boolean
    ) {
        this.client = client
        this.allowBots = allowBots
        numberOfPlayersChosen = list
        chosenColorProperty.set(availableColors[0])
        gameManager.setMessageProducedHandler(client::sendMessageToServer)
        gameManager.setGameEventHandler(this::handleGameEvent)
//        gameManager.game.corners[0] = 0
//        gameManager.game.corners[1] = 3
        this.gameManager = gameManager

    }

    private fun handleGameEvent(event: GameManager.Event) {
        when (event) {
            GameManager.Event.GameStarted -> runLater { startGame() }
            GameManager.Event.TurnStarted -> runLater { enableControls() }
            GameManager.Event.AvailableMovesChanged -> runLater { boardViewAdapter.highlightPossibleMoves() }
            GameManager.Event.GameEndedInterrupted -> TODO()
            GameManager.Event.GameEndedConcluded -> TODO()
            GameManager.Event.PlayerLeft -> TODO()
            GameManager.Event.MoveDone -> runLater { boardViewAdapter.performMove(gameManager.moveToBePerformed!!) }
        }
    }

    fun getBoard(): Pane {
        boardViewAdapter = BoardViewAdapter(gameManager, availableColors, chosenColorProperty)
        //gameManager.setMessageProducedHandler(boardViewAdapter::redrawBoard)
        val pane = boardViewAdapter.getBoard()
        pane.prefWidthProperty().bind(view.root.widthProperty())
        pane.prefHeightProperty().bind(view.root.heightProperty())
        return pane
    }

    fun endTurn() {
        boardViewAdapter.chosenMove?.let {
            gameManager.endTurn(it)
        } ?: pass()
        disableControls()
    }

    fun pass() {
        gameManager.pass()
        disableControls()
    }

    private fun disableControls() {
        runLater {
            view.root.center.isDisable = true
            view.endTurnButton.isDisable = true
            view.passButton.isDisable = true
            view.endTurnButton.text = "WAITING"
            boardViewAdapter.clearAllHighlights()
        }
    }

    private fun enableControls() {
        runLater {
            view.root.center.isDisable = false
            view.passButton.isDisable = false
            view.endTurnButton.isDisable = false
            view.endTurnButton.text = "END TURN"
        }
    }

    fun exitGame() {
        gameManager.exitGame()
        view.replaceWith<AppMenuView>()
    }

    fun startGame() {
        val board = getBoard()
        view.root.top.isVisible = true
        view.root.bottom.isVisible = true
        view.root.center = board
    }

    fun makeMove(move: HexMove) {
        client.sendMessageToServer(ChineseCheckersGameMessage.MoveRequested(move))
    }

    fun performReadyClicked() {
        with(view) {
            readyButton.isDisable = true
            root.center = vbox {
                alignment = Pos.CENTER
                addClass(Styles.gamePanel)
                text("waiting for other players to join game...") { addClass(Styles.label) }
            }
        }
        client.sendMessageToServer(ChineseCheckerServerMessage.GameRequest(numberOfPlayersChosen, allowBots!!))
    }
}