package client.ui

import client.model.CommunicationManager
import client.model.GameManager
import common.Player
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.Controller

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
        redirectToNextView(true)
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
        redirectToNextView(false)
    }

    fun redirectToNextView(allowBots: Boolean) {
        val gameManager = GameManager(player)
        client.addObserverFunction(gameManager::onMessageReceived)
        val list = numberOfPlayersChosenProperties.filter { it.value.get() }.map { it.key }
        find<GameViewController>().initClientAndList(client, gameManager, list, allowBots)
        view.replaceWith<AppGameView>()
    }
//    fun serverReturnedGameHandler(message: Message) {
//        if (message is ChineseCheckersGameMessage.GameAssigned) {
//            val gameManager: GameManager = GameManager(
//                player,
//                message.game
//            )
//            client.addObserverFunction(gameManager::onMessageReceived)
//            val list = numberOfPlayersChosenProperties.filter { it.value.get() }.map { it.key }
//            runLater {
//                find<GameViewController>().initClientAndList(client, gameManager, list)
//                client.removeObserverFunction(this::serverReturnedGameHandler)
//                view.replaceWith<AppGameView>()
//            }
//        }
//    }
}