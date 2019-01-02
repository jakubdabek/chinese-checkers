package client.ui

import client.model.GameManager
import client.model.GameScope
import common.HexMove
import common.chinesecheckers.ChineseCheckerServerMessage
import common.chinesecheckers.ChineseCheckersGameMessage
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.stage.WindowEvent
import tornadofx.*

class GameViewController : Controller() {
    override val scope get() = super.scope as GameScope
    private val client get() = scope.client
    private val gameManager get() = scope.gameManager
    private val numberOfPlayersChosen get() = scope.chosenPlayerQuantities
    private lateinit var boardViewAdapter: BoardViewAdapter
    private val view: AppGameView by inject()
    val availableColors =
        listOf<Color>(Color.RED, Color.GREEN, Color.YELLOW, Color.DARKVIOLET, Color.ORANGE, Color.DARKBLUE)
    val chosenColorProperty: SimpleObjectProperty<Paint> = SimpleObjectProperty(Color.DARKSLATEGREY)
    var chosenColor: Paint? by chosenColorProperty

    internal fun initClientAndList() {
        chosenColor = availableColors[0]
        client.addObserverFunction(gameManager::onMessageReceived)
        client.addOnErrorHandler(this::onCommunicationErrorHandler)
        gameManager.setMessageProducedHandler(client::sendMessageToServer)
        gameManager.setGameEventHandler(this::handleGameEvent)
//        gameManager.game.corners[0] = 0
//        gameManager.game.corners[1] = 3
    }

    private fun onCommunicationErrorHandler(fatal: Boolean,exception: Exception?) {
        val errorWindow = Alert(Alert.AlertType.ERROR)
        errorWindow.headerText = null
        errorWindow.contentText = "An error has occured.\n"
        exception?.let { errorWindow.contentText = "An error has occured.\n"+exception.message }
        errorWindow.showAndWait()
        exitGame()
        if (fatal) {
            System.exit(0)
        }
    }

    private fun handleGameEvent(event: GameManager.Event) {
        when (event) {
            GameManager.Event.GameStarted -> runLater { startGame() }
            GameManager.Event.TurnStarted -> runLater { enableControls() }
            GameManager.Event.AvailableMovesChanged -> runLater { boardViewAdapter.highlightPossibleMoves() }
            GameManager.Event.GameEndedInterrupted -> runLater { gameInterruptedHandler() }
            GameManager.Event.GameEndedConcluded -> gameEndedConcludedHandler()
            GameManager.Event.PlayerLeftLobby -> Unit
            GameManager.Event.PlayerJoined -> Unit
            GameManager.Event.MoveDone -> runLater { boardViewAdapter.performMove(gameManager.moveToBePerformed!!) }
        }
    }

    private fun gameEndedConcludedHandler() {
        runLater {
            view.showGameResult(gameManager.leaderBoard,gameManager.player)
        }
    }

    private fun gameInterruptedHandler() {
        val errorWindow = Alert(Alert.AlertType.ERROR)
        errorWindow.headerText = null
        errorWindow.contentText = "Someone has left the game.\nGame is ended.\n"
        errorWindow.showAndWait()
        exitGame()
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
            disableControls()
        }
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
            view.footer.isVisible = false
            boardViewAdapter.clearAllHighlights()
        }
    }

    private fun enableControls() {
        runLater {
            view.root.center.isDisable = false
            view.passButton.isDisable = false
            view.endTurnButton.isDisable = false
            view.endTurnButton.text = "END TURN"
            view.footer.isVisible = true
        }
    }

    var previousOnCloseRequestHandler: EventHandler<WindowEvent>? = null

    fun exitGame() {
        gameManager.exitGame()
        client.removeObserverFunction(gameManager::onMessageReceived)
        client.removeErrorHandler(this::onCommunicationErrorHandler)
        previousOnCloseRequestHandler?.let { primaryStage.onCloseRequest = it }
        view.replaceWith(find<AppMenuView>(scope.parentScope))
        scope.deregister()
    }

    fun startGame() {
        val board = getBoard()
        view.root.top.isVisible = true
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
        previousOnCloseRequestHandler = primaryStage.onCloseRequest
        primaryStage.onCloseRequest = EventHandler {
            gameManager.exitGame()
            previousOnCloseRequestHandler?.handle(it)
        }
        client.sendMessageToServer(ChineseCheckerServerMessage.GameRequest(numberOfPlayersChosen, scope.allowBots))
    }
}