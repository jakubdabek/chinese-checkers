package utility

import kotlin.random.Random


fun Random.Default.nextUniqueInt(existing: Collection<Int>) =
    generateSequence { nextInt() }.first { it !in existing }