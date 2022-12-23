package day23

import day23.LookupDirection.*
import readInput
import readTestInput
import kotlin.LazyThreadSafetyMode.NONE

private data class ElfPosition(val x: Int, val y: Int) {

    val northernNeighbors by lazy(mode = NONE) {
        listOf(
            ElfPosition(x - 1, y - 1),
            ElfPosition(x, y - 1),
            ElfPosition(x + 1, y - 1),
        )
    }

    val southernNeighbors by lazy(mode = NONE) {
        listOf(
            ElfPosition(x - 1, y + 1),
            ElfPosition(x, y + 1),
            ElfPosition(x + 1, y + 1),
        )
    }

    val westernNeighbors by lazy(mode = NONE) {
        listOf(
            ElfPosition(x - 1, y - 1),
            ElfPosition(x - 1, y),
            ElfPosition(x - 1, y + 1),
        )
    }

    val easternNeighbors by lazy(mode = NONE) {
        listOf(
            ElfPosition(x + 1, y - 1),
            ElfPosition(x + 1, y),
            ElfPosition(x + 1, y + 1),
        )
    }
}

private fun ElfPosition.neighborPositionsTo(direction: LookupDirection) = when (direction) {
    NORTH -> northernNeighbors
    SOUTH -> southernNeighbors
    WEST -> westernNeighbors
    EAST -> easternNeighbors
}

private fun ElfPosition.hasNeighborTo(direction: LookupDirection, elfPositions: List<ElfPosition>): Boolean =
    neighborPositionsTo(direction).any { neighborPosition -> neighborPosition in elfPositions }

private fun ElfPosition.hasNeighbor(elfPositions: List<ElfPosition>): Boolean =
    LookupDirection.values().any { direction -> hasNeighborTo(direction, elfPositions) }

private fun List<String>.toElfPositions() = flatMapIndexed { y: Int, row: String ->
    row.mapIndexedNotNull { x, cell ->
        if (cell == '#') ElfPosition(x, y) else null
    }
}

private sealed interface Action {
    data class Stay(val elfPosition: ElfPosition) : Action
    data class Move(val from: ElfPosition, val to: ElfPosition) : Action
}

private fun List<ElfPosition>.diffuse(rounds: Int): Pair<List<ElfPosition>, Int> {
    val lookupOrder = listOf(NORTH, SOUTH, WEST, EAST)
    var elfPositions = this

    var roundsApplied = 0
    while (roundsApplied < rounds) {
        val rotationIndex = roundsApplied % lookupOrder.size
        val roundLookupOrder = lookupOrder.drop(rotationIndex) + lookupOrder.take(rotationIndex)
        roundsApplied += 1

        val (stayActions, moveActions) = calculateActionsToPropose(elfPositions, roundLookupOrder)

        if (stayActions.size == elfPositions.size) {
            break
        }

        elfPositions = executeActions(moveActions, stayActions)
    }

    return elfPositions to roundsApplied
}

private fun calculateActionsToPropose(
    elfPositions: List<ElfPosition>,
    lookupOrder: List<LookupDirection>
): Pair<List<Action.Stay>, List<Action.Move>> {
    val actions = elfPositions.map { elfPosition ->
        when {
            elfPosition.hasNeighbor(elfPositions) -> {
                lookupOrder
                    .firstOrNull { direction -> !elfPosition.hasNeighborTo(direction, elfPositions) }
                    ?.let { direction -> elfPosition.movingTo(direction) }
                    ?: Action.Stay(elfPosition)
            }

            else -> Action.Stay(elfPosition)
        }
    }

    return actions.filterIsInstance<Action.Stay>() to actions.filterIsInstance<Action.Move>()
}

private fun executeActions(
    moveActions: List<Action.Move>,
    stayActions: List<Action.Stay>
): List<ElfPosition> {
    val positionsAfterMoveActions = moveActions
        .groupBy { move -> move.to }
        .flatMap { (_, movesToPosition) ->
            if (movesToPosition.size == 1) listOf(movesToPosition.single().to)
            else movesToPosition.map { it.from }
        }

    return stayActions.map { it.elfPosition } + positionsAfterMoveActions
}

private fun ElfPosition.movingTo(direction: LookupDirection): Action.Move =
    Action.Move(
        from = this,
        to = neighborPositionsTo(direction)[1]
    )

private enum class LookupDirection { NORTH, EAST, SOUTH, WEST }

private fun List<ElfPosition>.determineEmptyGroundTiles(): Int {
    val elfPositions = this

    val minX = elfPositions.minOf { it.x }
    val maxX = elfPositions.maxOf { it.x }
    val minY = elfPositions.minOf { it.y }
    val maxY = elfPositions.maxOf { it.y }

    val area = (maxX - minX + 1) * (maxY - minY + 1)

    return area - elfPositions.size
}

private fun part1(input: List<String>): Int {
    val elfPositions = input.toElfPositions()

    val (finalElfPositions, _) = elfPositions.diffuse(rounds = 10)

    return finalElfPositions.determineEmptyGroundTiles()
}

private fun part2(input: List<String>): Int {
    val elfPositions = input.toElfPositions()

    val (_, roundsNeeded) = elfPositions.diffuse(rounds = Int.MAX_VALUE)

    return roundsNeeded
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day23")
    check(part1(testInput) == 110)
    check(part2(testInput) == 20)

    val input = readInput("Day23")
    println(part1(input))
    println(part2(input))
}
