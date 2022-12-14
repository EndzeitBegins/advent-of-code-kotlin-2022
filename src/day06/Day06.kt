package day06

import readInput
import readTestInput

private fun String.findStartIndexOfMarker(markerLength: Int): Int = this
    .windowed(size = markerLength, step = 1)
    .indexOfFirst { sequence -> sequence.toSet().size == markerLength }

private fun part1(input: String): Int {
    val markerLength = 4

    return input.findStartIndexOfMarker(markerLength) + markerLength
}

private fun part2(input: String): Int {
    val markerLength = 14

    return input.findStartIndexOfMarker(markerLength) + markerLength
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day06").single()
    check(part1(testInput) == 11)
    check(part2(testInput) == 26)

    val input = readInput("Day06").single()
    println(part1(input))
    println(part2(input))
}
