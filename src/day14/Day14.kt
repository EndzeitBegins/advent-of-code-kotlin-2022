package day14

import readInput
import readTestInput

private data class Coordinate(val x: Int, val y: Int)

private val Set<Coordinate>.minX get() = minOf { coordinate -> coordinate.x }
private val Set<Coordinate>.minY get() = minOf { coordinate -> coordinate.y }
private val Set<Coordinate>.maxX get() = maxOf { coordinate -> coordinate.x }
private val Set<Coordinate>.maxY get() = maxOf { coordinate -> coordinate.y }

private fun List<String>.parseRockCoordinates(): Set<Coordinate> {
    return this.flatMap { rockFormation ->
        rockFormation
            .split(" -> ")
            .map { rawCoordinate ->
                val (x, y) = rawCoordinate.split(",")

                Coordinate(x.toInt(), y.toInt())
            }
            .windowed(size = 2, step = 1, partialWindows = false) {
                val (start, end) = it

                if (start.x == end.x) {
                    (minOf(start.y, end.y)..maxOf(start.y, end.y)).map { y -> Coordinate(start.x, y) }
                } else {
                    (minOf(start.x, end.x)..maxOf(start.x, end.x)).map { x -> Coordinate(x, start.y) }
                }
            }
            .flatten()
    }.toSet()
}

private fun leftUnderOf(coordinate: Coordinate): Coordinate = Coordinate(x = coordinate.x - 1, y = coordinate.y + 1)
private fun underOf(coordinate: Coordinate): Coordinate = Coordinate(x = coordinate.x, y = coordinate.y + 1)
private fun rightUnderOf(coordinate: Coordinate): Coordinate = Coordinate(x = coordinate.x + 1, y = coordinate.y + 1)

private enum class CaveParticle {
    AIR, ROCK, RESTING_SAND
}
private typealias CaveMap = Map<Coordinate, CaveParticle>

private fun Set<Coordinate>.toCaveMap(): CaveMap {
    val rockCoordinates = this

    return buildMap {
        val minY = rockCoordinates.minY.coerceAtMost(0)

        for (x in rockCoordinates.minX..rockCoordinates.maxX) {
            for (y in minY..rockCoordinates.maxY) {
                val coordinate = Coordinate(x = x, y = y)
                val particle = if (coordinate in rockCoordinates) CaveParticle.ROCK else CaveParticle.AIR

                put(coordinate, particle)
            }
        }
    }
}

private val CaveMap.minX get() = this.keys.minX
private val CaveMap.minY get() = this.keys.minY
private val CaveMap.maxX get() = this.keys.maxX
private val CaveMap.maxY get() = this.keys.maxY

private fun CaveMap.with(coordinate: Coordinate, particle: CaveParticle): CaveMap =
    buildMap {
        putAll(this@with)
        put(coordinate, particle)
    }

private fun CaveMap.withSandInserted(startCoordinate: Coordinate, floorAt: Int?): Pair<CaveMap, Coordinate?> {
    val caveMap = this
    val defaultOrNull: (key: Coordinate) -> CaveParticle? = if (floorAt == null) { _ -> null } else { coordinate ->
        if (coordinate.y == floorAt) CaveParticle.ROCK else CaveParticle.AIR
    }

    var x = startCoordinate.x
    var y = startCoordinate.y

    while (true) {
        val coordinate = Coordinate(x = x, y = y)

        val particleAtCoordinate = caveMap[coordinate] ?: defaultOrNull(coordinate)

        val positionUnder = underOf(coordinate)
        val particleUnder = caveMap[positionUnder] ?: defaultOrNull(positionUnder)

        val positionLeftUnder = leftUnderOf(coordinate)
        val particleLeftUnder = caveMap[positionLeftUnder] ?: defaultOrNull(positionLeftUnder)

        val positionRightUnder = rightUnderOf(coordinate)
        val particleRightUnder = caveMap[positionRightUnder] ?: defaultOrNull(positionRightUnder)

        when {
            particleAtCoordinate in setOf(CaveParticle.RESTING_SAND, CaveParticle.ROCK) ->
                return caveMap to null

            particleUnder == null ->
                return caveMap to null

            particleUnder == CaveParticle.AIR -> {
                y += 1
            }

            particleLeftUnder == null ->
                return caveMap to null

            particleLeftUnder == CaveParticle.AIR -> {
                x -= 1
                y += 1
            }

            particleRightUnder == null ->
                return caveMap to null

            particleRightUnder == CaveParticle.AIR -> {
                x += 1
                y += 1
            }

            else ->
                return caveMap.with(coordinate, CaveParticle.RESTING_SAND) to coordinate
        }
    }
}

private fun CaveMap.asPrintout(): String {
    val caveMap = this

    return buildString {
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val particle = when (caveMap[Coordinate(x, y)] ?: CaveParticle.AIR) {
                    CaveParticle.AIR -> '.'
                    CaveParticle.ROCK -> '#'
                    CaveParticle.RESTING_SAND -> 'o'
                }

                append(particle)
            }
            appendLine()
        }
    }
}

private fun CaveMap.fillWithSandFrom(coordinate: Coordinate, floorAt: Int? = null): Int {
    var computedCaveMap: CaveMap = this

    var insertions = 0
    while (true) {
        val result = computedCaveMap.withSandInserted(coordinate, floorAt)
        val targetPosition = result.second
        computedCaveMap = result.first

        if (targetPosition == null)
            break

        insertions += 1
    }

    return insertions
}

private val fillCoordinate = Coordinate(x = 500, y = 0)

private fun part1(input: List<String>): Int {
    val rockPositions = input.parseRockCoordinates()
    val caveMap: CaveMap = rockPositions.toCaveMap()

    return caveMap.fillWithSandFrom(coordinate = fillCoordinate)
}

private fun part2(input: List<String>): Int {
    val rockPositions = input.parseRockCoordinates()
    val caveMap: CaveMap = rockPositions.toCaveMap()

    return caveMap.fillWithSandFrom(coordinate = fillCoordinate, floorAt = rockPositions.maxY + 2)
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day14")
    check(part1(testInput) == 24)
    check(part2(testInput) == 93)

    val input = readInput("Day14")
    println(part1(input))
    println(part2(input))
}
