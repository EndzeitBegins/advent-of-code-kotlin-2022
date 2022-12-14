package day05

import readInput
import readTestInput

private fun List<String>.parse(): Pair<List<String>, List<String>> {
    val stackNumberLineIndex = this.indexOfFirst { line -> line.contains("""\d""".toRegex()) }

    val stacksDefinitions = this.take(stackNumberLineIndex + 1)
    val rawInstructions = this.drop(stackNumberLineIndex + 2)

    return stacksDefinitions to rawInstructions
}

private data class MoveInstruction(
    val fromIndex: Int,
    val toIndex: Int,
    val amount: Int,
)

private val instructionRegex = """move (\d+) from (\d+) to (\d+)""".toRegex()

private fun String.toMoveInstruction(): MoveInstruction {
    val (amount, from, to) = checkNotNull(instructionRegex.matchEntire(this)).destructured

    return MoveInstruction(
        fromIndex = from.toInt() - 1,
        toIndex = to.toInt() - 1,
        amount = amount.toInt()
    )
}

private fun Array<ArrayDeque<Char>>.retrieveTopCrates() =
    joinToString(separator = "") { "${it.removeLast()}" }

private fun List<String>.toStacks(): Array<ArrayDeque<Char>> {
    val stackDefinitions = this
    val stackAmount = stackDefinitions
        .last()
        .replace("""^.+\s+(\d+)\s*$""".toRegex()) { it.groupValues[1] }
        .toInt()

    val stacks = Array(stackAmount) { ArrayDeque<Char>() }

    for (crateLayer in stackDefinitions.dropLast(1)) {
        for (index in 0 until stackAmount) {
            val crateDefinition = crateLayer.drop(index * 4).take(3)

            if (crateDefinition.startsWith('[')) {
                stacks[index].addFirst(crateDefinition[1])
            }
        }
    }

    return stacks
}

private fun ArrayDeque<Char>.moveTo(target: ArrayDeque<Char>, amount: Int) {
    val source = this
    repeat(amount) {
        target.addLast(source.removeLast())
    }
}

private fun ArrayDeque<Char>.moveBulkTo(target: ArrayDeque<Char>, amount: Int) {
    val source = this

    val cache = mutableListOf<Char>()
    repeat(amount) {
        cache.add(source.removeLast())
    }
    for (cachedCrate in cache.asReversed()) {
        target.addLast(cachedCrate)
    }
}

private fun part1(input: List<String>): String {
    val (stacksDefinitions, rawInstructions) = input.parse()

    val stacks = stacksDefinitions.toStacks()
    val instructions = rawInstructions.map { rawInstruction -> rawInstruction.toMoveInstruction() }

    for (instruction in instructions) {
        val source = stacks[instruction.fromIndex]
        val target = stacks[instruction.toIndex]

        source.moveTo(target = target, amount = instruction.amount)
    }

    return stacks.retrieveTopCrates()
}

private fun part2(input: List<String>): String {
    val (stacksDefinitions, rawInstructions) = input.parse()

    val stacks = stacksDefinitions.toStacks()
    val instructions = rawInstructions.map { rawInstruction -> rawInstruction.toMoveInstruction() }

    for (instruction in instructions) {
        val source = stacks[instruction.fromIndex]
        val target = stacks[instruction.toIndex]

        source.moveBulkTo(target = target, amount = instruction.amount)
    }

    return stacks.retrieveTopCrates()
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day05")
    check(part1(testInput) == "CMZ")
    check(part2(testInput) == "MCD")

    val input = readInput("Day05")
    println(part1(input))
    println(part2(input))
}
