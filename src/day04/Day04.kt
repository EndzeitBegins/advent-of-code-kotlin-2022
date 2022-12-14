package day04

import readInput
import readTestInput

private fun List<String>.asElfPairSequence(): Sequence<Pair<String, String>> = this
    .asSequence()
    .map { elfPair ->
        val (firstElf, secondElf) = elfPair.split(",")

        firstElf to secondElf
    }

private fun Sequence<Pair<String, String>>.toSectionRangePair(): Sequence<Pair<IntRange, IntRange>> = this
    .map { (firstElf, secondElf) -> firstElf.toSectionRange() to secondElf.toSectionRange() }

private fun String.toSectionRange(): IntRange {
    val rangeValues = split("-").map { it.toInt() }

    return rangeValues[0]..rangeValues[1]
}

private fun IntRange.contains(other: IntRange): Boolean =
    this.first <= other.first && this.last >= other.last

private fun IntRange.overlaps(other: IntRange): Boolean =
    this.last >= other.first && this.first <= other.last

private fun part1(input: List<String>): Int {
    return input
        .asElfPairSequence()
        .toSectionRangePair()
        .count { (sectionRangeA, sectionRangeB) ->
            sectionRangeA.contains(sectionRangeB) || sectionRangeB.contains(sectionRangeA)
        }
}

private fun part2(input: List<String>): Int {
    return input
        .asElfPairSequence()
        .toSectionRangePair()
        .count { (sectionRangeA, sectionRangeB) ->
            sectionRangeA.overlaps(sectionRangeB)
        }
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day04")
    check(part1(testInput) == 2)
    check(part2(testInput) == 4)

    val input = readInput("Day04")
    println(part1(input))
    println(part2(input))
}
