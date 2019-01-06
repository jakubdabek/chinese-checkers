package common.chinesecheckers

import common.HexCoord
import common.HexMove
import common.SixSidedStarBoard
import common.neighbours
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class RuleSetTest {

    @Test
    fun `correct very simple movement rule`() {
        val board = SixSidedStarBoard(5)
        val ruleset = buildRuleset {
            addMovementRule { _, _ ->
                listOf(HexMove(listOf(HexCoord(1, 1) to HexCoord(1, 2))))
            }
        }
        Assertions.assertTrue(ruleset.checkMove(board, HexMove(listOf(HexCoord(1, 1) to HexCoord(1, 2)))))
        Assertions.assertFalse(ruleset.checkMove(board, HexMove(listOf(HexCoord(1, 1) to HexCoord(1, 1)))))
        Assertions.assertFalse(ruleset.checkMove(board, HexMove(listOf(HexCoord(1, 2) to HexCoord(1, 2)))))
        Assertions.assertFalse(ruleset.checkMove(board, HexMove(listOf(HexCoord(1, 2) to HexCoord(1, 1)))))
    }

    @Test
    fun `correct simple movement rule`() {
        val board = SixSidedStarBoard(5)
        val origin = HexCoord(0, 0, 0)
        val ruleset = buildRuleset {
            addMovementRule { _, position ->
                if (position == origin)
                    origin.neighbours.map { HexMove(listOf(origin to it)) }
                else
                    listOf()
            }
        }
        Assertions.assertEquals(
            origin.neighbours.map { HexMove(listOf(origin to it)) }.toSet(),
            ruleset.getPossibleMoves(board, origin).toSet()
        )

        Assertions.assertEquals(0, ruleset.getPossibleMoves(board, HexCoord(1, 1)).size)
        Assertions.assertEquals(0, ruleset.getPossibleMoves(board, HexCoord(5, 5)).size)
        Assertions.assertEquals(0, ruleset.getPossibleMoves(board, HexCoord(6, -8)).size)

        origin.neighbours.map { HexMove(listOf(origin to it)) }.forEach {
            Assertions.assertTrue(ruleset.checkMove(board, it))
        }
    }

    @Test
    fun `correct movement rule with forbidden moves`() {
        val board = SixSidedStarBoard(5)
        val origin = HexCoord(0, 0, 0)
        val neighbours = origin.neighbours
        val forbiddenMoves = setOf(HexMove(listOf(origin to neighbours[2])), HexMove(listOf(origin to neighbours[4])))
        val ruleset = buildRuleset {
            addMovementRule { _, position ->
                if (position == origin)
                    neighbours.map { HexMove(listOf(origin to it)) }
                else
                    listOf()
            }

            addForbiddenMovementRule { _, move -> move in forbiddenMoves }
        }
        Assertions.assertEquals(
            origin.neighbours.map { HexMove(listOf(origin to it)) }.toSet() - forbiddenMoves,
            ruleset.getPossibleMoves(board, origin).toSet()
        )

        Assertions.assertEquals(0, ruleset.getPossibleMoves(board, HexCoord(1, 1)).size)
        Assertions.assertEquals(0, ruleset.getPossibleMoves(board, HexCoord(5, 5)).size)
        Assertions.assertEquals(0, ruleset.getPossibleMoves(board, HexCoord(6, -8)).size)

        origin.neighbours.map { HexMove(listOf(origin to it)) }.filterNot { it in forbiddenMoves }.forEach {
            Assertions.assertTrue(ruleset.checkMove(board, it))
        }
    }
}