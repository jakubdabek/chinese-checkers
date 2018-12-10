package common.chinesecheckers

import common.Message


sealed class ChineseCheckerServerMessage : Message {
    data class GameRequest(val playersCount: List<Int>, val allowBots: Boolean) : ChineseCheckerServerMessage() {
        override val content = Pair(playersCount, allowBots)
    }
}
