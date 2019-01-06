package server

import common.GameResult
import common.HexMove
import common.Player
import common.chinesecheckers.ChineseCheckersGameMessage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class GameManagerTest {

    @Test
    fun `adding correct number of players`() {
        GameManager.usableCorners.keys.forEach { maxPlayers ->
            val gameManager = GameManager(maxPlayers, false) {}
            repeat(maxPlayers) {
                Assertions.assertTrue(gameManager.tryAddPlayer(Player(it, "$it")))
            }
            Assertions.assertFalse(gameManager.tryAddPlayer(Player(maxPlayers, "$maxPlayers")))
        }
    }

    @Test
    fun `correct events on adding players`() {
        GameManager.usableCorners.entries.forEach { (maxPlayers, corners) ->
            var counter = 0
            lateinit var gameManager: GameManager
            gameManager = GameManager(maxPlayers, false) { responses ->
                when {
                    counter < maxPlayers * 2 && counter % 2 == 0 -> {
                        Assertions.assertEquals(
                            listOf(Response(
                                ChineseCheckersGameMessage.GameAssigned(gameManager.game),
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
                            gameManager.game.corners
                        )
                        Assertions.assertEquals(
                            (0 until maxPlayers).map { i ->
                                Response(
                                    ChineseCheckersGameMessage.GameStarted(
                                        gameManager.game.corners
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
                gameManager.tryAddPlayer(Player(it, "$it"))
            }
            gameManager.tryAddPlayer(Player(maxPlayers, "$maxPlayers"))
            Assertions.assertEquals(maxPlayers * 2 + 2, counter)
        }
    }

    @Test
    fun `error on adding bot to people-only game`() {
        val gameManager = GameManager(2, false) {}
        Assertions.assertThrows(AssertionError::class.java) {
            gameManager.addBot(Player(1, "Bot#1"))
        }
    }

    @Test
    fun `error on adding more than 1 player to a game with bots enabled`() {
        GameManager.usableCorners.keys.forEach { maxPlayers ->
            val gameManager = GameManager(maxPlayers, true) {}
            Assertions.assertTrue(gameManager.tryAddPlayer(Player(1, "1")))
            Assertions.assertFalse(gameManager.tryAddPlayer(Player(2, "2")))
        }
    }

    @Test
    fun `adding correct number of bots`() {
        GameManager.usableCorners.keys.forEach { maxPlayers ->
            val gameManager = GameManager(maxPlayers, true) {}
            repeat(maxPlayers) {
                Assertions.assertDoesNotThrow {
                    gameManager.addBot(Player(it, "$it"))
                }
            }
            Assertions.assertThrows(AssertionError::class.java) {
                gameManager.addBot(Player(maxPlayers, "$maxPlayers"))
            }
        }
    }

    @Test
    fun `correct events on removing players from a partially filled game`() {
        GameManager.usableCorners.keys.forEach { maxPlayers ->
            var counter = 0
            val maxPlayersAdded = maxPlayers - 1
            val lastPlayer = Player(maxPlayersAdded - 1, "${maxPlayersAdded - 1}")
            lateinit var gameManager: GameManager
            gameManager = GameManager(maxPlayers, false) { responses ->
                if (counter == maxPlayersAdded * 2) {
                    Assertions.assertEquals(
                        (0 until (maxPlayersAdded - 1)).map { i ->
                            Response(
                                ChineseCheckersGameMessage.PlayerLeftLobby(lastPlayer),
                                Player(i, "$i")
                            )
                        }.toSet(),
                        responses.toSet()
                    )
                }
                counter++
            }
            repeat(maxPlayersAdded) {
                gameManager.tryAddPlayer(Player(it, "$it"))
            }
            gameManager.removePlayer(lastPlayer)
            Assertions.assertEquals(maxPlayersAdded * 2 + 1, counter)
        }
    }

    @Test
    fun `error on incorrect message`() {
        val gameManager = GameManager(2, false) {}
        val player = Player(1, "1")
        gameManager.tryAddPlayer(player)
        listOf(
            ChineseCheckersGameMessage.GameAssigned(gameManager.game),
            ChineseCheckersGameMessage.TurnStarted,
            ChineseCheckersGameMessage.GameStarted(gameManager.game.corners),
            ChineseCheckersGameMessage.PlayerJoined(player),
            ChineseCheckersGameMessage.PlayerLeftLobby(player),
            ChineseCheckersGameMessage.GameEnded(GameResult.Interrupted),
            ChineseCheckersGameMessage.MoveDone(HexMove(listOf())),
            ChineseCheckersGameMessage.MoveRejected,
            ChineseCheckersGameMessage.AvailableMoves(listOf())
        ).forEach {
            Assertions.assertThrows(NotImplementedError::class.java) {
                gameManager.handleGameMessage(it, player)
            }
        }

    }
}
