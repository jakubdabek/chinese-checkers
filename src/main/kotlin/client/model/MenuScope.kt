package client.model

import common.Player
import tornadofx.Scope


class MenuScope(
    val communicationManager: CommunicationManager,
    val player: Player
) : Scope()
