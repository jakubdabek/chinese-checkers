package server

import common.Player
import common.chinesecheckers.ChineseCheckersGameMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class GameManagerTest {

    private var gameManager: GameManager? = null

    @AfterEach
    fun cleanup() {
        gameManager = null
    }

    @Test
    fun `adding correct number of players`() {
        GameManager.usableCorners.keys.map { maxPlayers ->
            val gameManager = GameManager(maxPlayers, false) {}
            repeat(maxPlayers) {
                Assertions.assertTrue(gameManager.tryAddPlayer(Player(it, "$it")))
            }
            Assertions.assertFalse(gameManager.tryAddPlayer(Player(maxPlayers, "$maxPlayers")))
        }
    }

    @Test
    fun `correct events on adding players`() {
        GameManager.usableCorners.entries.map { (maxPlayers, corners) ->
            var counter = 0
            gameManager = GameManager(maxPlayers, false) { responses ->
                when {
                    counter < maxPlayers * 2 && counter % 2 == 0 -> {
                        Assertions.assertEquals(
                            listOf(Response(
                                ChineseCheckersGameMessage.GameAssigned(gameManager!!.game),
                                Player(counter / 2, "${counter / 2}")
                            )),
                            responses
                        )
                    }
                    counter < maxPlayers * 2 && counter % 2 == 1 -> {
                        Assertions.assertEquals(
                            (0 until counter / 2).map {
                                Response(
                                    ChineseCheckersGameMessage.PlayerJoined(Player(counter / 2, "${counter / 2}")),
                                    Player(it, "$it")
                                )
                            }.toSet(),
                            responses.toSet()
                        )
                    }
                    counter == maxPlayers * 2 -> {
                        Assertions.assertEquals(
                            (0 until maxPlayers).map { Player.Id(it) }.zip(corners).toMap(),
                            gameManager!!.game.corners
                        )
                        Assertions.assertEquals(
                            (0 until maxPlayers).map { i ->
                                Response(
                                    ChineseCheckersGameMessage.GameStarted(
                                        gameManager!!.game.corners
                                    ),
                                    Player(i, "$i")
                                )
                            }.toSet(),
                            responses.toSet()
                        )
                    }
                    else -> {
                        Assertions.assertEquals(
                            responses.singleOrNull()?.message,
                            ChineseCheckersGameMessage.TurnStarted
                        )
                    }
                }
                counter++
            }
            repeat(maxPlayers) {
                gameManager!!.tryAddPlayer(Player(it, "$it"))
            }
            gameManager!!.tryAddPlayer(Player(maxPlayers, "$maxPlayers"))
            Assertions.assertEquals(maxPlayers * 2 + 2, counter)
        }
    }
}
