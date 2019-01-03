package utility

import common.Player
import kotlin.random.Random


fun Random.Default.nextUniqueInt(existing: Collection<Int>) =
    generateSequence { nextInt() }.first { it !in existing }


operator fun Map<Player.Id, *>.get(key: Int) = get(Player.Id(key))
operator fun <T> MutableMap<Player.Id, T>.set(key: Int, value: T) = set(Player.Id(key), value)
