package client.ui

import client.model.GameManager
import client.model.GameScope
import client.model.MenuScope
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*


class MenuViewController : Controller() {
    private val view: AppMenuView by inject()
    override val scope get() = super.scope as MenuScope
    private val player get() = scope.player
    var numberOfPlayersChosenProperties =
        listOf(2,3,4,6).associateWith { SimpleBooleanProperty() }

    fun playWithComputerClickHandler() {
        redirectToNextView(true)
    }

    fun playWithHumanPlayersClickHandler() {
        redirectToNextView(false)
    }

    private fun redirectToNextView(allowBots: Boolean) {
        val gameManager = GameManager(player)
        val list = numberOfPlayersChosenProperties.filter { it.value.get() }.map { it.key }
        val scope = GameScope(scope, gameManager, list, allowBots)
        find<GameViewController>(scope).initClientAndList()
        view.replaceWith(find<AppGameView>(scope))
    }
}
