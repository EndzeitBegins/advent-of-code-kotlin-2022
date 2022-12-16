package day15

import readInput
import readTestInput
import kotlin.math.absoluteValue

private data class Position(val x: Int, val y: Int)

private data class Sensor(val position: Position) {
    constructor(x: Int, y: Int) : this(Position(x, y))
}

private data class Beacon(val position: Position) {
    constructor(x: Int, y: Int) : this(Position(x, y))
}

private data class ScannedArea(val center: Position, val radius: Int)

private fun distanceBetween(a: Position, b: Position): Int {
    return (a.x - b.x).absoluteValue + (a.y - b.y).absoluteValue
}

private infix fun Position.isIn(area: ScannedArea): Boolean {
    return distanceBetween(this, area.center) <= area.radius
}

private fun List<String>.toSensorBeaconPairs(): List<Pair<Sensor, Beacon>> {
    val regex = """Sensor at x=(-?\d+), y=(-?\d+): closest beacon is at x=(-?\d+), y=(-?\d+)""".toRegex()

    return map { line ->
        val result = regex.matchEntire(line)
        val (sensorX, sensorY, beaconX, beaconY) = checkNotNull(result).destructured

        Sensor(sensorX.toInt(), sensorY.toInt()) to Beacon(beaconX.toInt(), beaconY.toInt())
    }
}

private fun List<ScannedArea>.findNonScannedPosition(searchDistance: Int): Position {
    val scannedAreas = this

    for (scannedArea in scannedAreas) {
        val sensor = scannedArea.center
        val radius = scannedArea.radius
        val borderDistance = radius + 1

        for (offset in (0..borderDistance)) {
            val positionsToInspect = listOf(
                Position(sensor.x - borderDistance + offset, sensor.y + offset),
                Position(sensor.x - borderDistance + offset, sensor.y - offset),
                Position(sensor.x + borderDistance - offset, sensor.y + offset),
                Position(sensor.x + borderDistance - offset, sensor.y - offset),
            )

            val nonScannedPosition: Position? = positionsToInspect.firstOrNull { position->
                position.x >= 0 && position.y >= 0
                        && position.x <= searchDistance && position.y <= searchDistance
                        && scannedAreas.none { area -> position isIn area }
            }

            if (nonScannedPosition != null)
                return nonScannedPosition
        }
    }

    error("No non-scanned position found!")
}

private fun part1(input: List<String>, scanY: Int): Int {
    val sensorBeaconPairs = input.toSensorBeaconPairs()
    val scannedAreas = sensorBeaconPairs.map { (sensor, beacon) ->
        ScannedArea(sensor.position, distanceBetween(sensor.position, beacon.position))
    }

    val minX = scannedAreas.minOf { scannedArea -> scannedArea.center.x - scannedArea.radius }
    val maxX = scannedAreas.maxOf { scannedArea -> scannedArea.center.x + scannedArea.radius }

    val scannedPositions = (minX..maxX)
        .map { x -> Position(x = x, y = scanY) }
        .count { position ->
            sensorBeaconPairs.none { (_, beacon) -> beacon.position == position }
                    && scannedAreas.any { scannedArea -> position isIn scannedArea }
        }

    return scannedPositions
}

private fun part2(input: List<String>, searchDistance: Int): Long {
    val sensorBeaconPairs = input.toSensorBeaconPairs()
    val scannedAreas = sensorBeaconPairs.map { (sensor, beacon) ->
        ScannedArea(sensor.position, distanceBetween(sensor.position, beacon.position))
    }
    val nonScannedPosition = scannedAreas.findNonScannedPosition(searchDistance)

    return 4_000_000L * nonScannedPosition.x + nonScannedPosition.y
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day15")
    check(part1(testInput, scanY = 10) == 26)
    check(part2(testInput, searchDistance = 20) == 56_000_011L)

    val input = readInput("Day15")
    println(part1(input, scanY = 2_000_000))
    println(part2(input, searchDistance = 4_000_000))
}
