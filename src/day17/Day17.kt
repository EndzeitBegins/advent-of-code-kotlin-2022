package day17

import readInput
import readTestInput
import kotlin.math.min

private data class Position(val x: Int, val y: Int)

private fun p(x: Int, y: Int) = Position(x = x, y = y)

private sealed interface Shape {
    val partPositions: List<Position>

    fun fitsInto(chamber: Chamber, at: Position): Boolean {
        return partPositions.none { position ->
            val x = at.x + position.x
            val y = at.y + position.y

            x < 0 || x >= 7 || y < 0 || chamber[y][x]
        }
    }

    object Line : Shape {
        override val partPositions =
            listOf(p(x = 0, y = 0), p(x = 1, y = 0), p(x = 2, y = 0), p(x = 3, y = 0))
    }

    object Cross : Shape {
        override val partPositions =
            listOf(p(x = 0, y = 0), p(x = -1, y = 1), p(x = 0, y = 1), p(x = 1, y = 1), p(x = 0, y = 2))
    }

    object Boomerang : Shape {
        override val partPositions =
            listOf(p(x = 0, y = 0), p(x = 1, y = 0), p(x = 2, y = 0), p(x = 2, y = 1), p(x = 2, y = 2))
    }

    object Bar : Shape {
        override val partPositions =
            listOf(p(x = 0, y = 0), p(x = 0, y = 1), p(x = 0, y = 2), p(x = 0, y = 3))
    }

    object Cube : Shape {
        override val partPositions =
            listOf(p(x = 0, y = 0), p(x = 1, y = 0), p(x = 0, y = 1), p(x = 1, y = 1))
    }
}

enum class Jet { LEFT, RIGHT }

private fun List<String>.toJetSequence(): Sequence<Jet> {
    val jetDefinition = this.single()

    return sequence {
        while (true) {
            for (char in jetDefinition) {
                when (char) {
                    '<' -> yield(Jet.LEFT)
                    '>' -> yield(Jet.RIGHT)
                }
            }
        }
    }
}

private data class Rock(val position: Position, val shape: Shape) {
    val x: Int = position.x
    val y: Int = position.y

    fun moveLeft(chamber: Chamber): Rock =
        moveBy(offset = Position(x = -1, y = 0), chamber = chamber)

    fun moveRight(chamber: Chamber): Rock =
        moveBy(offset = Position(x = 1, y = 0), chamber = chamber)

    fun moveDown(chamber: Chamber): Rock =
        moveBy(offset = Position(x = 0, y = -1), chamber = chamber)

    private fun moveBy(offset: Position, chamber: Chamber): Rock {
        val targetPosition = Position(x + offset.x, y + offset.y)

        return if (shape.fitsInto(chamber = chamber, at = targetPosition)) {
            this.copy(position = targetPosition)
        } else this
    }

    fun placeInto(chamber: Chamber) {
        for (partPosition in shape.partPositions) {
            val x = x + partPosition.x
            val y = y + partPosition.y

            chamber[y][x] = true
        }
    }
}

private typealias Chamber = MutableList<BooleanArray>

private val Chamber.highestRockY: Int
    get() {
        for (i in indices.reversed()) {
            if (this[i].any { isBlocked -> isBlocked }) {
                return i
            }
        }
        return -1
    }

private fun Chamber.fillWithRowsUpTo(row: Int) {
    val chamber = this

    if (chamber.size <= row) {
        val missingRows = row - chamber.lastIndex

        repeat(missingRows) {
            chamber.add(BooleanArray(7))
        }
    }
}

private fun Chamber.insert(rock: Rock, jets: Iterator<Jet>) {
    val chamber = this

    val rockAtDestination = rock.moveToFinalPosition(chamber, jets)

    rockAtDestination.placeInto(chamber)
}

private fun Chamber.calculateHeight() =
    indexOfLast { row -> row.any { it } } + 1

private fun Chamber.asHexString() =
    this.joinToString(separator = "") { row ->
        val binaryRepresentation = row.joinToString(separator = "") { if (it) "1" else "0" }.toInt(2)
        binaryRepresentation.toString(16).padStart(2, '0')
    }

private fun Chamber.asPrintout(): String {
    return this.asReversed().joinToString(separator = "\n") { row ->
        row.joinToString(separator = "") { isBlocked ->
            if (isBlocked) "#" else "."
        }
    }
}

private fun simulateSpawningRocks(chamber: Chamber): Sequence<Rock> {
    var index = 0

    return sequence {
        while (true) {
            val yOfTallestRock = chamber.highestRockY

            chamber.fillWithRowsUpTo(yOfTallestRock + 10)

            val shape = when (index) {
                0 -> Shape.Line
                1 -> Shape.Cross
                2 -> Shape.Boomerang
                3 -> Shape.Bar
                4 -> Shape.Cube
                else -> error("Unknown shape index!")
            }
            val x = 2 - shape.partPositions.minOf { it.x }
            val y = yOfTallestRock + 4

            yield(Rock(position = p(x = x, y = y), shape = shape))

            index = (index + 1) % 5
        }
    }
}

private fun List<String>.simulateFallingRocks(rockCount: Int): Chamber {
    val input = this

    val chamber: Chamber = MutableList(1) { BooleanArray(7) }
    val jets = input.toJetSequence().iterator()
    val rocks = simulateSpawningRocks(chamber).take(rockCount)

    for (rock in rocks) {
        chamber.insert(rock, jets)
    }

    return chamber
}

private fun Rock.moveToFinalPosition(chamber: Chamber, jets: Iterator<Jet>): Rock {
    var movingRock = this

    var heightBeforeMoves: Int
    do {
        heightBeforeMoves = movingRock.y

        val jet = jets.next()
        movingRock = when (jet) {
            Jet.LEFT -> movingRock.moveLeft(chamber)
            Jet.RIGHT -> movingRock.moveRight(chamber)
        }

        movingRock = movingRock.moveDown(chamber)
    } while (heightBeforeMoves != movingRock.y)

    return movingRock
}

fun longestCommonPrefix(lhs: String, rhs: String): String {
    val n = min(lhs.length, rhs.length)

    for (i in 0 until n) {
        if (lhs[i] != rhs[i]) {
            return lhs.substring(0, i)
        }
    }

    return lhs.substring(0, n)
}

fun String.findLongestRepeatedString(): String {
    val suffixes = Array(length) { index ->
        substring(index, length)
    }
    suffixes.sort()

    var longestRepeatedSubstring = ""

    for (i in 0 until lastIndex) {
        val longestCommonPrefix = longestCommonPrefix(suffixes[i], suffixes[i + 1])

        if (longestCommonPrefix.length > longestRepeatedSubstring.length) {
            longestRepeatedSubstring = longestCommonPrefix
        }
    }

    return longestRepeatedSubstring
}

private fun part1(input: List<String>): Int {
    val chamber = input.simulateFallingRocks(2022)

    return chamber.calculateHeight()
}

private fun part2(input: List<String>): Long {
    val targetRocks = 1_000_000_000_000

    // search for repeating pattern in rocks, using first 10_000 rocks as sample
    val predictionChamber = input.simulateFallingRocks(10_000)
    val predictionBase = predictionChamber.asHexString()
    val repeatingPart = predictionBase.findLongestRepeatedString()
    val repeatingPattern = repeatingPart.take(repeatingPart.length - repeatingPart.findLongestRepeatedString().length)
    val firstRepetitionStart = predictionBase.indexOf(repeatingPattern)
    val nonRepeatingStart = predictionBase.take(firstRepetitionStart)

    // simulate again, to find
    // - tower height before pattern starts to repeat
    // - tower height of a single pattern repetition
    // - tower height of rocks missing, after rocks before pattern and rocks in N cycles of pattern repetition
    val jets = input.toJetSequence().iterator() // use iterator to prevent restarts
    val chamber: Chamber = MutableList(1) { BooleanArray(7) }
    val rocks = simulateSpawningRocks(chamber).iterator() // use iterator to prevent restarts

    var towerHeightBeforePatternRepeats: Int? = null
    var towerHeightInASinglePatternRepetition: Int? = null
    var towerHeightOfRocksAfterPatternRepetitions: Int = 0

    // find tower height before pattern starts to repeat
    var rocksFallenBeforePattern = 0L
    for (rock in rocks) {
        chamber.insert(rock, jets)
        rocksFallenBeforePattern += 1

        val hexChamber = chamber.asHexString()

        if (hexChamber.startsWith(nonRepeatingStart)) {
            towerHeightBeforePatternRepeats = chamber.calculateHeight()
            break
        }
    }
    checkNotNull(towerHeightBeforePatternRepeats)

    // find tower height of a single pattern repetition
    var rocksFallenDuringPattern = 0L
    for (rock in rocks) {
        chamber.insert(rock, jets)
        rocksFallenDuringPattern += 1

        val hexChamber = chamber.asHexString()

        if (hexChamber.contains(repeatingPattern)) {
            towerHeightInASinglePatternRepetition = chamber.calculateHeight() - towerHeightBeforePatternRepeats
            break
        }
    }
    checkNotNull(towerHeightInASinglePatternRepetition)

    // find tower height of rocks missing, after rocks before pattern and rocks in N cycles of pattern repetition
    val cyclesOfRepetitionNeeded = (targetRocks - rocksFallenBeforePattern) / rocksFallenDuringPattern
    val rocksNeededAfterPatternRepitition =
        targetRocks - rocksFallenBeforePattern - (rocksFallenDuringPattern * cyclesOfRepetitionNeeded)

    if (rocksNeededAfterPatternRepitition > 0) {
        var rocksFallenAfterPatternRepetition = 0L
        for (rock in rocks) {
            chamber.insert(rock, jets)
            rocksFallenAfterPatternRepetition += 1

            if (rocksFallenAfterPatternRepetition >= rocksNeededAfterPatternRepitition) {
                towerHeightOfRocksAfterPatternRepetitions = chamber.calculateHeight() - towerHeightBeforePatternRepeats - towerHeightInASinglePatternRepetition
                break
            }
        }
    }

    return towerHeightBeforePatternRepeats +
            (cyclesOfRepetitionNeeded * towerHeightInASinglePatternRepetition) +
            towerHeightOfRocksAfterPatternRepetitions
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day17")
    check(part1(testInput) == 3068)
    check(part2(testInput) == 1514285714288)

    val input = readInput("Day17")
    println(part1(input))
    println(part2(input))
}
