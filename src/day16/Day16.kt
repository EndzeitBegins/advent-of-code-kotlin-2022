package day16

import readInput
import readTestInput

private data class Valve(
    val flowRate: Long,
    val tunnelsTo: Set<String>,
)

private typealias Valves = Map<String, Valve>

private data class State(
    val position: Valve,
    val openValves: Set<Valve>,
    val totalFlowRate: Long,
    val releasedPressure: Long,
)

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

private data class Option(
    val position: String,
    val valvesOpened: Set<String>,
    val releasedPressureAtEnd: Long,
    val timeLeft: Int,
)

private fun Option.availableOptions(valves: Valves, shortestRoutes: Map<String, Map<String, Int>>): List<Option> {
    val option = this

    val possibleTargets = shortestRoutes.getValue(option.position)
        .filterNot { (valveName, moveCost) ->
            val possibleTargetValve = valves.getValue(valveName)
            val cannotOpen = option.timeLeft < (moveCost + 2)

            valveName in option.valvesOpened || possibleTargetValve.flowRate == 0L ||  cannotOpen
        }

    if (option.timeLeft <= 0 || possibleTargets.isEmpty()) {
        return listOf(option)
    }

    return possibleTargets.flatMap { (targetValveName, moveCost) ->
        val targetValve = valves.getValue(targetValveName)
        val openCost = moveCost + 1
        val releasedPressureFromValveAtEnd = (option.timeLeft - openCost) * targetValve.flowRate

        Option(
            position = targetValveName,
            valvesOpened = option.valvesOpened + targetValveName,
            releasedPressureAtEnd = option.releasedPressureAtEnd + releasedPressureFromValveAtEnd,
            timeLeft = option.timeLeft -  openCost
        ).availableOptions(valves, shortestRoutes)
    }
}

private fun part1(input: List<String>): Long {
    val valves = input.toValves()
    val shortestRoutes = valves.findShortestRoutes()

    val initialOption = Option(
        position = "AA",
        valvesOpened = emptySet(),
        releasedPressureAtEnd = 0L,
        timeLeft = 30,
    )

    val options: List<Option> = listOf(initialOption)
        .flatMap { option -> option.availableOptions(valves, shortestRoutes) }

    return options.maxOf { option -> option.releasedPressureAtEnd }
}

private fun part2(input: List<String>): Long {
    return input.size.toLong()
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day16")
    check(part1(testInput) == 1651L)
//    check(part2(testInput) == 1707L)

    val input = readInput("Day16")
    println(part1(input))
    println(part2(input))
}
