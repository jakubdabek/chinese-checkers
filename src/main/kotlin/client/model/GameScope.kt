package client.model

import tornadofx.*

class GameScope(
    val parentScope: MenuScope,
    val gameManager: GameManager,
    val chosenPlayerQuantities: List<Int>,
    val allowBots: Boolean
) : Scope() {
    val client get() = parentScope.communicationManager
}