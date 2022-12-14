package day01

import readInput
import readTestInput

private fun List<String>.toSnackSumSequence(): Sequence<Int> = sequence {
    var sum = 0

    for (item in this@toSnackSumSequence) {
        if (item.isBlank()) {
            this.yield(sum)
            sum = 0
        } else {
            sum += item.toInt()
        }
    }
    this.yield(sum)
}

private fun part1(input: List<String>): Int =
    input
        .toSnackSumSequence()
        .max()

private fun part2(input: List<String>): Int =
    input
        .toSnackSumSequence()
        .sortedDescending()
        .take(3)
        .sum()

fun main() {

    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day01")
    check(part1(testInput) == 24000)
    check(part2(testInput) == 45000)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
