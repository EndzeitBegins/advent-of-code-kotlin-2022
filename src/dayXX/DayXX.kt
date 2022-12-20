package dayXX

import readInput
import readTestInput

private fun part1(input: List<String>): Int {
    return input.size
}

private fun part2(input: List<String>): Int {
    return input.size
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("DayXX")
    check(part1(testInput) == 1)
//    check(part2(testInput) == 1)

    val input = readInput("DayXX")
    println(part1(input))
    println(part2(input))
}
