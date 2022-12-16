package day16

import readInput
import readTestInput

private data class Valve(
    val flowRate: Long,
    val tunnelsTo: Set<String>,
)

private typealias Valves = Map<String, Valve>

private fun List<String>.toValves(): Valves {
    val regex = """Valve (\w+) has flow rate=(\d+);.*valves? (\w+(, \w+)*)""".toRegex()

    return associate { line ->
        val (name, rate, tunnels) = checkNotNull(regex.matchEntire(line)).destructured

        name to Valve(flowRate = rate.toLong(), tunnelsTo = tunnels.split(", ").toSet())
    }
}

private fun Valves.findShortestRoutes(): Map<String, Map<String, Int>> {
    val valves = this

    var shortestRoutes: Map<String, Map<String, Int>> = mapValues { (_, valve) ->
        valve.tunnelsTo.associateWith { 1 }
    }

    repeat(valves.size) {
        shortestRoutes = shortestRoutes.mapValues { (_, routesFromValve) ->
            val allRoutes = routesFromValve
                .flatMap { existingRoute ->
                    val (middleTarget, cost) = existingRoute

                    shortestRoutes.getValue(middleTarget).map { (subTarget, subCost) ->
                        subTarget to cost + subCost
                    }
                } + routesFromValve.map { it.key to it.value }

            allRoutes
                .groupBy { it.first }
                .mapValues { (_, costs) -> costs.minOf { it.second } }
                .toMap()
        }

        if (shortestRoutes.all { routes -> routes.value.size == valves.size }) {
            return shortestRoutes
        }
    }

    return shortestRoutes
}

private data class PossibleMove(
    val position: String,
    val valvesOpened: Set<String>,
    val releasedPressureAtEnd: Long,
    val timeLeft: Int,
)

private fun findPossibleTargets(
    shortestRoutes: Map<String, Map<String, Int>>,
    valves: Valves,
    position: String,
    timeLeft: Int,
    valvesOpened: Set<String>
) = shortestRoutes.getValue(position)
    .filterNot { (valveName, moveCost) ->
        val possibleTargetValve = valves.getValue(valveName)
        val cannotOpen = timeLeft < (moveCost + 2)

        valveName in valvesOpened || possibleTargetValve.flowRate == 0L || cannotOpen
    }

private fun PossibleMove.availableMoves(
    valves: Valves,
    shortestRoutes: Map<String, Map<String, Int>>
): List<PossibleMove> {
    val move = this

    val possibleTargets = findPossibleTargets(
        shortestRoutes = shortestRoutes,
        valves = valves,
        position = move.position,
        timeLeft = move.timeLeft,
        valvesOpened = move.valvesOpened
    )

    if (move.timeLeft <= 0 || possibleTargets.isEmpty()) {
        return listOf(move)
    }

    return possibleTargets.flatMap { (targetValveName, moveCost) ->
        val targetValve = valves.getValue(targetValveName)
        val openCost = moveCost + 1
        val releasedPressureFromValveAtEnd = (move.timeLeft - openCost) * targetValve.flowRate

        PossibleMove(
            position = targetValveName,
            valvesOpened = move.valvesOpened + targetValveName,
            releasedPressureAtEnd = move.releasedPressureAtEnd + releasedPressureFromValveAtEnd,
            timeLeft = move.timeLeft - openCost
        ).availableMoves(valves, shortestRoutes)
    } + move
}

private fun part1(input: List<String>): Long {
    val valves = input.toValves()
    val shortestRoutes = valves.findShortestRoutes()
        .filterKeys { valveName -> valveName == "AA" || valves.getValue(valveName).flowRate > 0 }

    val initialMove = PossibleMove(
        position = "AA",
        valvesOpened = emptySet(),
        releasedPressureAtEnd = 0L,
        timeLeft = 30,
    )

    val possibleMoves: List<PossibleMove> = listOf(initialMove)
        .flatMap { move -> move.availableMoves(valves, shortestRoutes) }

    return possibleMoves.maxOf { move -> move.releasedPressureAtEnd }
}

private fun part2(input: List<String>): Long {
    val valves = input.toValves()
    val shortestRoutes = valves.findShortestRoutes()
        .filterKeys { valveName -> valveName == "AA" || valves.getValue(valveName).flowRate > 0 }

    val initialMove = PossibleMove(
        position = "AA",
        valvesOpened = emptySet(),
        releasedPressureAtEnd = 0L,
        timeLeft = 26,
    )

    val possibleMoves: List<PossibleMove> = listOf(initialMove)
        .flatMap { move -> move.availableMoves(valves, shortestRoutes) }

    var maximumPressureRelease = 0L
    for (i in possibleMoves.indices) {
        val myMove = possibleMoves[i]
        val myOpenedValves = myMove.valvesOpened
        val myPressureRelease = myMove.releasedPressureAtEnd

        for (j in i..possibleMoves.lastIndex) {
            val elephantMove = possibleMoves[j]
            val elephantOpenedValves = elephantMove.valvesOpened
            val elephantPressureRelease = elephantMove.releasedPressureAtEnd
            val combinedPressureRelease = myPressureRelease + elephantPressureRelease

            if (maximumPressureRelease < combinedPressureRelease &&
                myOpenedValves.none { myValve -> myValve in elephantOpenedValves }) {
                maximumPressureRelease = combinedPressureRelease
            }
        }
    }

    return maximumPressureRelease
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day16")
    check(part1(testInput) == 1651L)
    check(part2(testInput) == 1707L)

    val input = readInput("Day16")
    println(part1(input))
    println(part2(input))
}
