package client.model

import common.Message
import common.Player
import common.SixSidedStarBoard
import common.chinesecheckers.ChineseCheckersGame
import javafx.scene.paint.Color

class GameManager(val player: Player,val game: ChineseCheckersGame) {
    val playerId = player.id
    //val colors = mutableMapOf<Int, Color>()
    private val observersFunctions = mutableListOf<(Message) -> Unit>()

    fun addObserverFunction(function: (Message) -> Unit) {
        observersFunctions.add(function)
    }
}