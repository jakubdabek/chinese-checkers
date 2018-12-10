package common

sealed class GameResult {
    object Interrupted : GameResult()
    class Ended(val leaderboard: List<Player>) : GameResult()
}
