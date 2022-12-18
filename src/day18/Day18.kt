package day18

import readInput
import readTestInput

private data class Coordinate(val x: Int, val y: Int, val z: Int)
private typealias LavaCube = Coordinate
private typealias LavaDroplet = Set<LavaCube>

private fun List<String>.toLavaDroplet(): LavaDroplet = this
    .map { line ->
        val (x, y, z) = line.split(",")

        LavaCube(x.toInt(), y.toInt(), z.toInt())
    }
    .toSet()

private val Coordinate.sides: List<Coordinate>
    get() = listOf(
        Coordinate(x = x - 1, y = y, z = z),
        Coordinate(x = x + 1, y = y, z = z),
        Coordinate(x = x, y = y - 1, z = z),
        Coordinate(x = x, y = y + 1, z = z),
        Coordinate(x = x, y = y, z = z - 1),
        Coordinate(x = x, y = y, z = z + 1),
    )

private fun LavaCube.findVisibleSides(lavaDroplet: LavaDroplet): List<Coordinate> =
    sides.filterNot { sideCube -> sideCube in lavaDroplet }

private fun part1(input: List<String>): Int {
    val lavaDroplet = input.toLavaDroplet()

    val lavaDropletOutsideSurfaceArea = lavaDroplet
        .flatMap { lavaCube -> lavaCube.findVisibleSides(lavaDroplet) }

    return lavaDropletOutsideSurfaceArea.size
}

private fun part2(input: List<String>): Int {
    val lavaDroplet = input.toLavaDroplet()

    val visibleSides = lavaDroplet
        .flatMap { lavaCube -> lavaCube.findVisibleSides(lavaDroplet) }

    val minX = lavaDroplet.minOf { it.x } - 1
    val minY = lavaDroplet.minOf { it.y } - 1
    val minZ = lavaDroplet.minOf { it.z } - 1
    val maxX = lavaDroplet.maxOf { it.x } + 1
    val maxY = lavaDroplet.maxOf { it.y } + 1
    val maxZ = lavaDroplet.maxOf { it.z } + 1

    val spaceAroundLavaDroplet = mutableListOf<Coordinate>()
    val workingQueue = mutableListOf(Coordinate(x = minX, y = minY, z = minZ))
    while (workingQueue.isNotEmpty()) {
        val active = workingQueue.removeFirst()

        spaceAroundLavaDroplet.add(active)

        val candidates = active.sides
            .asSequence()
            .filterNot { it.x < minX || it.x > maxX }
            .filterNot { it.y < minY || it.y > maxY }
            .filterNot { it.z < minZ || it.z > maxZ }
            .filterNot { it in workingQueue }
            .filterNot { it in spaceAroundLavaDroplet }
            .filterNot { it in lavaDroplet }

        for (candidate in candidates) {
            workingQueue.add(candidate)
        }
    }

    val lavaDropletOutsideSurfaceArea = visibleSides.filter { it in spaceAroundLavaDroplet }

    return lavaDropletOutsideSurfaceArea.size
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day18")
    check(part1(testInput) == 64)
    check(part2(testInput) == 58)

    val input = readInput("Day18")
    println(part1(input))
    println(part2(input))
}
