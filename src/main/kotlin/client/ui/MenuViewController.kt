package client.ui

import client.model.GameManager
import client.model.GameScope
import client.model.MenuScope
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*

class MenuViewController : Controller() {
    private val view: AppMenuView by inject()
    override val scope get() = super.scope as MenuScope
    private val client get() = scope.communicationManager
    private val player get() = scope.player
    var numberOfPlayersChosenProperties =
        listOf(2,3,4,6).associateWith { SimpleBooleanProperty() }

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
        val list = numberOfPlayersChosenProperties.filter { it.value.get() }.map { it.key }
        val scope = GameScope(scope, gameManager, list, allowBots)
        find<GameViewController>(scope).initClientAndList()
        view.replaceWith(find<AppGameView>(scope))
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