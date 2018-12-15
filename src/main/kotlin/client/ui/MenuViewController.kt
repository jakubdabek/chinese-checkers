package client.ui

import client.model.CommunicationManager
import client.model.GameManager
import common.Message
import common.Player
import common.chinesecheckers.ChineseCheckerServerMessage
import common.chinesecheckers.ChineseCheckersGameMessage
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import tornadofx.Controller
import tornadofx.runLater
import tornadofx.*

class MenuViewController : Controller() {
    private val view: AppMenuView by inject()
    private lateinit var client: CommunicationManager
    private lateinit var player: Player
    var numberOfPlayersChosenProperties =
        listOf(2,3,4,6).map { it to SimpleBooleanProperty() }.toMap()

    fun initCommunicationManager(manager: CommunicationManager, player: Player) {
        this.client = manager
        this.player = player
    }

    fun playWithComputerClickHandler() {
        view.rootVBox.children.clear()
        view.rootVBox.vbox {
            fitToParentSize()
            addClass(Styles.mainVBox)
            alignment = Pos.CENTER
            text("waiting for game") { addClass(Styles.label) }
        }
        client.addObserverFunction(this::serverReturnedGameHandler)
        val list = numberOfPlayersChosenProperties.filter { it.value.get() }.map { it.key }
        client.sendMessageToServer(ChineseCheckerServerMessage.GameRequest(list,false))
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
                player,
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