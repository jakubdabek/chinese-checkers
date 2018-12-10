package common.chinesecheckers

import common.Message
import common.Player


sealed class ChineseCheckersClientMessage : Message {
    data class ConnectionEstablished(val player: Player) : ChineseCheckersClientMessage() {
        override val content = player
    }
}
