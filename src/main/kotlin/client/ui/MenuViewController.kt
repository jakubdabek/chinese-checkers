package client.ui

import client.model.GameManager
import common.Player
import common.SixSidedStarBoard
import common.chinesecheckers.ChineseCheckersGame
import tornadofx.Controller

class MenuViewController : Controller() {
    private val view: AppMenuView by inject()
    private val gameManager: GameManager = GameManager(Player(0,"ania4"),
        ChineseCheckersGame(SixSidedStarBoard(5),
        mutableListOf(Player(0,"ania4"))))
    fun playWithComputerClickHandler() {
//        val c = find<GameViewController>()
//        val c2 = find<GameViewController>()
//        println("c: $c, c2: $c2")
//        val v1 = find<AppGameView>(AppGameView::board to common.SixSidedStarBoard(4))
//        val v2 = find<AppGameView>()
//        println("${v1.board}")
        find<GameViewController>().initGameManager(gameManager)
        view.replaceWith<AppGameView>()
        //TODO("not implemented")
    }
    fun playWithHumanPlayersClickHandler() {
        view.replaceWith<AppGameView>()
        //TODO("not implemented")
    }
}