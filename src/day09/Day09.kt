package day09

import readInput
import readTestInput
import kotlin.math.absoluteValue

private data class Position(val x: Int, val y: Int)

private fun List<String>.asSteps(): List<Char> =
    flatMap { command ->
        List(size = command.substring(2).toInt()) { command.first() }
    }

private fun generateRope(size: Int): List<Position> =
    List(size = size) { Position(x = 0, y = 0) }

private fun Position.take(step: Char): Position {
    return when (step) {
        'L' -> copy(x = x - 1)
        'R' -> copy(x = x + 1)
        'U' -> copy(y = y + 1)
        'D' -> copy(y = y - 1)

        else -> error("Invalid step '$step'!")
    }
}

private fun Position.follow(other: Position): Position {
    val xOffset = this.x - other.x
    val yOffset = this.y - other.y

    return if (xOffset.absoluteValue >= 2 || yOffset.absoluteValue >= 2) {
        val adjustXBy = when {
            xOffset < 0 -> 1
            xOffset > 0 -> -1
            else -> 0
        }
        val adjustYBy = when {
            yOffset < 0 -> 1
            yOffset > 0 -> -1
            else -> 0
        }

        Position(x = x + adjustXBy, y = y + adjustYBy)
    } else this
}

private fun List<Position>.simulate(steps: List<Char>): Set<Position> {
    val simulatedRope = this.toMutableList()
    val tailPositions = mutableSetOf(simulatedRope.last())

    for (step in steps) {
        simulatedRope[0] = simulatedRope[0].take(step)

        for (i in simulatedRope.indices.drop(1)) {
            simulatedRope[i] = simulatedRope[i].follow(simulatedRope[i - 1])
        }

        tailPositions += simulatedRope.last()
    }

    return tailPositions
}

private fun part1(input: List<String>): Int {
    val steps = input.asSteps()
    val rope = generateRope(size = 2)
    val tailPositions = rope.simulate(steps = steps)

    return tailPositions.size
}

private fun part2(input: List<String>): Int {
    val steps = input.asSteps()
    val rope = generateRope(size = 10)
    val tailPositions = rope.simulate(steps = steps)

    return tailPositions.size
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day09")
    check(part1(testInput) == 13)
    check(part2(testInput) == 1)

    val input = readInput("Day09")
    println(part1(input))
    println(part2(input))
}
