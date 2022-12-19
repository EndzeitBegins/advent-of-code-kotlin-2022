package day19

import readInput
import readTestInput

private data class Blueprint(
    val id: Int,
    val oreRobotOreCost: Int,
    val clayRobotOreCost: Int,
    val obsidianRobotOreCost: Int,
    val obsidianRobotClayCost: Int,
    val geodeRobotOreCost: Int,
    val geodeRobotObsidianCost: Int,
) {
    val maxOreUsagePossible: Int = listOf(
        oreRobotOreCost,
        clayRobotOreCost,
        obsidianRobotOreCost,
        geodeRobotOreCost,
    ).max()
    val maxClayUsagePossible: Int = obsidianRobotClayCost
    val maxObsidianUsagePossible: Int = geodeRobotObsidianCost
}

private val blueprintRegex =
    """Blueprint (\d+): Each ore robot costs (\d+) ore. Each clay robot costs (\d+) ore. Each obsidian robot costs (\d+) ore and (\d+) clay. Each geode robot costs (\d+) ore and (\d+) obsidian.""".toRegex()

private fun List<String>.toBlueprints(): List<Blueprint> = this.map { line ->
    val (
        blueprintId,
        oreRobotOreCost,
        clayRobotOreCost,
        obsidianRobotOreCost,
        obsidianRobotClayCost,
        geodeRobotOreCost,
        geodeRobotObsidianCost,
    ) = checkNotNull(blueprintRegex.matchEntire(line)).destructured

    Blueprint(
        id = blueprintId.toInt(),
        oreRobotOreCost = oreRobotOreCost.toInt(),
        clayRobotOreCost = clayRobotOreCost.toInt(),
        obsidianRobotOreCost = obsidianRobotOreCost.toInt(),
        obsidianRobotClayCost = obsidianRobotClayCost.toInt(),
        geodeRobotOreCost = geodeRobotOreCost.toInt(),
        geodeRobotObsidianCost = geodeRobotObsidianCost.toInt(),
    )
}

private fun Blueprint.simulateOutcomes(minutes: Int): List<EventNode> {
    val initialEventNode = EventNode(
        blueprint = this,
        inventory = initialInventory,
        robotTeam = initialRobotTeam
    )

    var eventNodes = listOf(initialEventNode)
    repeat(minutes) {
        eventNodes = eventNodes
            .flatMap { eventNode -> eventNode.possibleActions() }
            .filterForMostSensibleActions(minutes * 125)
    }

    return eventNodes
}

private fun Blueprint.calculateOptimalOutcome(minutes: Int): EventNode =
    simulateOutcomes(minutes).maxBy { it.inventory.geodes }

private fun Blueprint.calculateQualityLevel(minutes: Int) =
    id * calculateOptimalOutcome(minutes).inventory.geodes

private data class Inventory(
    val ore: Int,
    val clay: Int,
    val obsidian: Int,
    val geodes: Int,
)

private fun Inventory.withWorkFrom(
    robotTeam: RobotTeam,
    usingOre: Int = 0,
    usingClay: Int = 0,
    usingObsidian: Int = 0
) = Inventory(
    ore = ore + robotTeam.oreRobots - usingOre,
    clay = clay + robotTeam.clayRobots - usingClay,
    obsidian = obsidian + robotTeam.obsidianRobots - usingObsidian,
    geodes = geodes + robotTeam.geodeRobots,
)

private val initialInventory = Inventory(
    ore = 0,
    clay = 0,
    obsidian = 0,
    geodes = 0
)

private data class RobotTeam(
    val oreRobots: Int,
    val clayRobots: Int,
    val obsidianRobots: Int,
    val geodeRobots: Int,
)

private fun RobotTeam.enlargedBy(
    oreRobots: Int = 0,
    clayRobots: Int = 0,
    obsidianRobots: Int = 0,
    geodeRobots: Int = 0
) = RobotTeam(
    oreRobots = this.oreRobots + oreRobots,
    clayRobots = this.clayRobots + clayRobots,
    obsidianRobots = this.obsidianRobots + obsidianRobots,
    geodeRobots = this.geodeRobots + geodeRobots
)

private val initialRobotTeam = RobotTeam(
    oreRobots = 1,
    clayRobots = 0,
    obsidianRobots = 0,
    geodeRobots = 0
)

private data class EventNode(
    val blueprint: Blueprint,
    val inventory: Inventory,
    val robotTeam: RobotTeam,
)

private fun EventNode.possibleActions(): List<EventNode> {
    if (inventory.ore >= blueprint.geodeRobotOreCost && inventory.obsidian >= blueprint.geodeRobotObsidianCost) {
        return listOf(
            EventNode(
                blueprint = blueprint,
                inventory = inventory.withWorkFrom(
                    robotTeam,
                    usingOre = blueprint.geodeRobotOreCost,
                    usingObsidian = blueprint.geodeRobotObsidianCost,
                ),
                robotTeam = robotTeam.enlargedBy(geodeRobots = 1)
            )
        )
    }

    val possibleActions = mutableListOf(
        EventNode(
            blueprint = blueprint,
            inventory = inventory.withWorkFrom(robotTeam),
            robotTeam = robotTeam,
        )
    )

    if (robotTeam.obsidianRobots > blueprint.maxObsidianUsagePossible)
        return possibleActions

    if (inventory.ore >= blueprint.obsidianRobotOreCost &&
        inventory.clay >= blueprint.obsidianRobotClayCost
    ) {
        possibleActions += EventNode(
            blueprint = blueprint,
            inventory = inventory.withWorkFrom(
                robotTeam,
                usingOre = blueprint.obsidianRobotOreCost,
                usingClay = blueprint.obsidianRobotClayCost,
            ),
            robotTeam = robotTeam.enlargedBy(obsidianRobots = 1)
        )
    }

    if (robotTeam.clayRobots > blueprint.maxClayUsagePossible)
        return possibleActions

    if (inventory.ore >= blueprint.clayRobotOreCost) {
        possibleActions += EventNode(
            blueprint = blueprint,
            inventory = inventory.withWorkFrom(robotTeam, usingOre = blueprint.clayRobotOreCost),
            robotTeam = robotTeam.enlargedBy(clayRobots = 1)
        )
    }

    if (robotTeam.oreRobots > blueprint.maxOreUsagePossible)
        return possibleActions

    if (inventory.ore >= blueprint.oreRobotOreCost) {
        possibleActions += EventNode(
            blueprint = blueprint,
            inventory = inventory.withWorkFrom(robotTeam, usingOre = blueprint.oreRobotOreCost),
            robotTeam = robotTeam.enlargedBy(oreRobots = 1)
        )
    }

    return possibleActions
}

private fun List<EventNode>.filterForMostSensibleActions(limit: Int): List<EventNode> {
    val blueprint = this.first().blueprint

    val oreWorth = 1
    val oreRobotWorth = blueprint.oreRobotOreCost + 1
    val clayWorth = blueprint.clayRobotOreCost
    val clayRobotWorth = clayWorth + 1
    val obsidianWorth = blueprint.obsidianRobotOreCost * oreWorth + blueprint.obsidianRobotClayCost * clayWorth
    val obsidianRobotWorth = obsidianWorth + 1
    val geodeWorth = blueprint.geodeRobotOreCost * oreWorth + blueprint.geodeRobotObsidianCost * obsidianWorth
    val geodeRobotWorth = geodeWorth + 1

    fun scoreOf(eventNode: EventNode) = eventNode.inventory.ore * oreWorth +
            eventNode.robotTeam.oreRobots * oreRobotWorth +
            eventNode.inventory.clay * clayWorth +
            eventNode.robotTeam.clayRobots * clayRobotWorth +
            eventNode.inventory.obsidian * obsidianWorth +
            eventNode.robotTeam.obsidianRobots * obsidianRobotWorth +
            eventNode.inventory.geodes * geodeWorth +
            eventNode.robotTeam.geodeRobots * geodeRobotWorth

    return this
        .sortedByDescending { eventNode -> scoreOf(eventNode) }
        .take(limit)
}

private fun part1(input: List<String>): Int {
    val blueprints = input.toBlueprints()

    return blueprints.sumOf { blueprint ->
        blueprint.calculateQualityLevel(minutes = 24)
    }
}

private fun part2(input: List<String>): Int {
    val blueprints = input.toBlueprints().take(3)

    return blueprints
        .map { blueprint -> blueprint.calculateOptimalOutcome(minutes = 32).inventory.geodes }
        .reduce(Int::times)
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day19")
    check(part1(testInput) == 33)
    check(part2(testInput) == 56 * 62)

    val input = readInput("Day19")
    println(part1(input))
    println(part2(input))
}
