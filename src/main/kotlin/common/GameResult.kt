package common

import java.io.Serializable


sealed class GameResult : Serializable {
    object Interrupted : GameResult()
    class Ended(val leaderboard: List<Player>) : GameResult()
}
