package day24

import readInput
import readTestInput


private data class Position(var x: Int, var y: Int)
private enum class Direction { LEFT, RIGHT, UP, DOWN }
private data class Blizzard(val position: Position, val direction: Direction)

private data class BlizzardValley(
    val blizzards: List<Blizzard>,
    val height: Int,
    val width: Int,
)

private fun List<String>.scanBlizzardsInValley(): BlizzardValley {
    val relevantRows = this.drop(1).dropLast(1)

    val blizzards = relevantRows.flatMapIndexed { y, row ->
        row.drop(1).dropLast(1).mapIndexedNotNull { x, block ->
            val direction = when (block) {
                '<' -> Direction.LEFT
                '>' -> Direction.RIGHT
                '^' -> Direction.UP
                'v' -> Direction.DOWN
                else -> null
            }

            if (direction != null) {
                Blizzard(Position(x = x, y = y), direction)
            } else null
        }
    }

    return BlizzardValley(
        blizzards,
        size - 2,
        first().length - 2,
    )
}

private fun lcm(lhs: Int, rhs: Int): Int {
    return if (lhs == 0 || rhs == 0) {
        0
    } else {
        (lhs * rhs) / gcd(lhs, rhs)
    }
}

private tailrec fun gcd(lhs: Int, rhs: Int): Int {
    return if (lhs == 0 || rhs == 0) {
        lhs + rhs
    } else {
        val largerValue = maxOf(lhs, rhs)
        val smallerValue = minOf(lhs, rhs)

        gcd(largerValue.mod(smallerValue), smallerValue)
    }
}

private fun BlizzardValley.freePositionsEachMinute(): Sequence<Array<Position>> {
    val freePositionsEveryMinute = mutableListOf<Array<Position>>()

    val roundsUntilCycleRepeats = lcm(height, width)
    var cycles = 0

    return sequence {
        while (true) {
            val freePositions = if (cycles <= freePositionsEveryMinute.lastIndex) {
                freePositionsEveryMinute[cycles]
            } else {
                adjustBlizzardPositionsForNextMinute()

                val freePositions = findAvailablePositions().toTypedArray()
                freePositionsEveryMinute += freePositions

                freePositions
            }
            cycles = (cycles + 1).mod(roundsUntilCycleRepeats)

            yield(freePositions)
        }
    }
}

private fun BlizzardValley.adjustBlizzardPositionsForNextMinute() {
    for (blizzard in blizzards) {
        when (blizzard.direction) {
            Direction.LEFT -> {
                blizzard.position.x = (blizzard.position.x - 1).mod(width)
            }

            Direction.RIGHT -> {
                blizzard.position.x = (blizzard.position.x + 1).mod(width)
            }

            Direction.UP -> {
                blizzard.position.y = (blizzard.position.y - 1).mod(height)
            }

            Direction.DOWN -> {
                blizzard.position.y = (blizzard.position.y + 1).mod(height)
            }
        }
    }
}

private fun BlizzardValley.findAvailablePositions(): List<Position> {
    val positionsTakenByBlizzards = blizzards.groupBy { it.position }.keys
    val freePositions = buildList {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val position = Position(x, y)

                if (position !in positionsTakenByBlizzards) {
                    add(position)
                }
            }
        }
    }
    return freePositions
}

private fun calculateMinimumDistance(
    blizzardValley: BlizzardValley,
    freePositionsEachMinute: Iterator<Array<Position>>,
    start: Position,
    target: Position,
): Int {
    var rounds = 0
    var positionsReachable = Array(blizzardValley.height) { BooleanArray(blizzardValley.width) }
    while (!positionsReachable[target.y][target.x]) {
        val freePositions = freePositionsEachMinute.next()

        val positionsReachableNextMinute = Array(blizzardValley.height) { BooleanArray(blizzardValley.width) }
        for (freePosition in freePositions) {
            val freeX = freePosition.x
            val freeY = freePosition.y

            val neighbors = listOf(
                freeX - 1 to freeY, // left
                freeX + 1 to freeY, // right
                freeX to freeY, // center
                freeX to freeY - 1, // up
                freeX to freeY + 1, // down
            )
            val isReachable = neighbors.any { (checkX, checkY) ->
                val sanitizedX = checkX.coerceIn(0, blizzardValley.width - 1)
                val sanitizedY = checkY.coerceIn(0, blizzardValley.height - 1)

                positionsReachable[sanitizedY][sanitizedX]
            }

            if (isReachable) {
                positionsReachableNextMinute[freeY][freeX] = true
            }
        }

        // mark entry as reachable, if there is no blizzard
        if (start in freePositions) {
            positionsReachableNextMinute[start.y][start.x] = true
        }

        positionsReachable = positionsReachableNextMinute
        rounds += 1
    }

    return rounds
}

private fun part1(input: List<String>): Int {
    val blizzardValley = input.scanBlizzardsInValley()

    val freePositionsEachMinute = blizzardValley.freePositionsEachMinute().iterator()

    val rounds = calculateMinimumDistance(
        blizzardValley = blizzardValley,
        freePositionsEachMinute = freePositionsEachMinute,
        start = Position(x = 0, y = 0),
        target = Position(x = blizzardValley.width - 1, y = blizzardValley.height - 1)
    )

    return rounds + 1
}


private fun part2(input: List<String>): Int {
    val blizzardValley = input.scanBlizzardsInValley()

    val freePositionsEachMinute = blizzardValley.freePositionsEachMinute().iterator()

    val firstTripToTarget = calculateMinimumDistance(
        blizzardValley = blizzardValley,
        freePositionsEachMinute = freePositionsEachMinute,
        start = Position(x = 0, y = 0),
        target = Position(x = blizzardValley.width - 1, y = blizzardValley.height - 1),
    ) + 1
    freePositionsEachMinute.next()

    val tripToStartForSnack = calculateMinimumDistance(
        blizzardValley = blizzardValley,
        freePositionsEachMinute = freePositionsEachMinute,
        start = Position(x = blizzardValley.width - 1, y = blizzardValley.height - 1),
        target = Position(x = 0, y = 0),
    ) + 1
    freePositionsEachMinute.next()

    val secondTripToTarget = calculateMinimumDistance(
        blizzardValley = blizzardValley,
        freePositionsEachMinute = freePositionsEachMinute,
        start = Position(x = 0, y = 0),
        target = Position(x = blizzardValley.width - 1, y = blizzardValley.height - 1),
    ) + 1

    return firstTripToTarget + tripToStartForSnack + secondTripToTarget
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day24")
    check(part1(testInput) == 18)
    check(part2(testInput) == 54)

    val input = readInput("Day24")
    println(part1(input))
    println(part2(input))
}
