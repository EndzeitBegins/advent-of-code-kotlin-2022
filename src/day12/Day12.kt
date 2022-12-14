package day12

import readInput
import readTestInput

private data class Node(
    val elevation: Int,
    val rawElevation: Char,
)

private data class Coordinate(val x: Int, val y: Int)

private infix fun Int.to(y: Int) = Coordinate(x = this, y = y)

private fun List<String>.toNodeMap(): Map<Coordinate, Node> =
    flatMapIndexed { y, row ->
        row.mapIndexed { x, elevation ->
            val coordinate = Coordinate(x, y)
            val node = Node(
                elevation = when (elevation) {
                    in 'a'..'z' -> elevation - 'a'
                    'S' -> 0
                    'E' -> 26
                    else -> error("Elevation '$elevation' is not supported!")
                },
                rawElevation = elevation,
            )

            coordinate to node
        }
    }.toMap()

private fun Node.canBeReachedFrom(node: Node): Boolean =
    this.elevation - node.elevation <= 1

private typealias Path = List<Node>

private fun Map<Coordinate, Node>.findShortestPaths(
    vararg startNodeCoordinates: Coordinate
): Map<Coordinate, Path?> {
    val shortestPaths: MutableMap<Coordinate, Path?> =
        this.mapValues { _ -> null }.toMutableMap()

    val nodesToScan = mutableListOf(*startNodeCoordinates)

    for (startNodeCoordinate in startNodeCoordinates) {
        shortestPaths[startNodeCoordinate] = emptyList()
    }

    while (nodesToScan.isNotEmpty()) {
        val nodeCoordinates = nodesToScan.removeFirst()
        val node = getValue(nodeCoordinates)

        val adjacentNodeCoordinates = listOf(
            nodeCoordinates.x - 1 to nodeCoordinates.y,
            nodeCoordinates.x + 1 to nodeCoordinates.y,
            nodeCoordinates.x to nodeCoordinates.y - 1,
            nodeCoordinates.x to nodeCoordinates.y + 1,
        )

        val path: Path = (shortestPaths[nodeCoordinates] ?: emptyList()) + node

        for (adjacentNodeCoordinate in adjacentNodeCoordinates) {
            val adjacentNode = get(adjacentNodeCoordinate)

            if (adjacentNode != null && adjacentNode.canBeReachedFrom(node)) {
                val currentPath = shortestPaths[adjacentNodeCoordinate]
                val currentDistance = currentPath?.size ?: Int.MAX_VALUE

                if (path.size < currentDistance) {
                    shortestPaths[adjacentNodeCoordinate] = path
                    nodesToScan.add(adjacentNodeCoordinate)
                }
            }
        }
    }

    return shortestPaths
}

private fun part1(input: List<String>): Int {
    val nodeMap = input.toNodeMap()

    val startNodeCoordinate = nodeMap.entries
        .single { (_, node) -> node.rawElevation == 'S' }
        .key
    val endNodeCoordinate = nodeMap.entries
        .single { (_, node) -> node.rawElevation == 'E' }
        .key

    val shortestPaths: Map<Coordinate, Path?> =
        nodeMap.findShortestPaths(startNodeCoordinate)

    return checkNotNull(shortestPaths[endNodeCoordinate.x to endNodeCoordinate.y]).size
}

private fun part2(input: List<String>): Int {
    val nodeMap = input.toNodeMap()

    val startNodeCoordinates = nodeMap.entries
        .filter { (_, node) -> node.rawElevation in setOf('S', 'a') }
        .map { it.key }
    val endNodeCoordinate = nodeMap.entries
        .single { (_, node) -> node.rawElevation == 'E' }
        .key

    val shortestPaths: Map<Coordinate, Path?> =
        nodeMap.findShortestPaths(*startNodeCoordinates.toTypedArray())

    return checkNotNull(shortestPaths[endNodeCoordinate.x to endNodeCoordinate.y]).size
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day12")
    check(part1(testInput) == 31)
    check(part2(testInput) == 29)

    val input = readInput("Day12")
    println(part1(input))
    println(part2(input))
}
