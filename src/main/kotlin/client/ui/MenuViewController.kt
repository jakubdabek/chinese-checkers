package client.ui

import client.model.CommunicationManager
import client.model.GameManager
import common.Message
import common.Player
import common.SixSidedStarBoard
import common.chinesecheckers.ChineseCheckerServerMessage
import common.chinesecheckers.ChineseCheckersGame
import common.chinesecheckers.ChineseCheckersGameMessage
import javafx.geometry.Pos
import tornadofx.Controller
import tornadofx.runLater
import tornadofx.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class MenuViewController : Controller() {
    private val view: AppMenuView by inject()
    private lateinit var client: CommunicationManager


    fun initCommunicationManager(manager: CommunicationManager) {
        client = manager
    }

    fun playWithComputerClickHandler() {
        //send game request
        //wait for server to call handler when received game
        view.rootVBox.children.clear()
        view.rootVBox.vbox {
            fitToParentSize()
            addClass(Styles.mainVBox)
            alignment = Pos.CENTER
            text("waiting for game") { addClass(Styles.label) }
        }
        client.addObserverFunction(this::serverReturnedGameHandler)
        client.sendMessageToServer(ChineseCheckerServerMessage.GameRequest(listOf(2),false))
//        thread {
//            sleep(500)
//            serverReturnedGameHandler(
//                ChineseCheckersGameMessage.GameAssigned(
//                    ChineseCheckersGame(
//                        SixSidedStarBoard(5),
//                        mutableListOf(Player(0, "ania4"), Player(1, "zosia34"))
//                    )
//                )
//            )
//        }
    }

    fun playWithHumanPlayersClickHandler() {
        //TODO("not implemented")
    }

    fun serverReturnedGameHandler(message: Message) {
        if (message is ChineseCheckersGameMessage.GameAssigned) {
            val gameManager: GameManager = GameManager(
                Player(0, "ania4"),
                message.game

            )
            client.addObserverFunction(gameManager::onMessageReceived)
            runLater {
                find<GameViewController>().initClientAndGameManager(client, gameManager)
                client.removeObserverFunction(this::serverReturnedGameHandler)
                view.replaceWith<AppGameView>()
            }
        }
    }
}