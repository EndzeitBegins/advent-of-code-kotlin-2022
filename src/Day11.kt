private data class Monkey(
    val items: MutableList<Long>,
    val operation: (initialWorryLevel: Long) -> Long,
    val nextMonkey: (worryLevel: Long) -> Int,
)

private fun List<String>.toMonkeys(withStressRelief: Boolean): List<Monkey> {
    val commonDivisor = this
        .filter { line -> line.contains("divisible by") }
        .map { it.substringAfter("divisible by ").toLong() }
        .reduce { lhs, rhs -> lhs * rhs }

    return this
        .windowed(size = 6, step = 7) { monkeyLines ->
            Monkey(
                items = monkeyLines.extractStartingItems().toMutableList(),
                operation = monkeyLines.extractOperation(
                    commonDivisor = commonDivisor,
                    withStressRelief = withStressRelief
                ),
                nextMonkey = monkeyLines.extractMonkeyTargets()
            )
        }
}

private fun List<String>.extractStartingItems() =
    this[1].substringAfter(':').split(',').map { it.trim().toLong() }

private fun List<String>.extractOperation(
    commonDivisor: Long,
    withStressRelief: Boolean
): (initialWorryLevel: Long) -> Long {
    val (lhsStr, operatorStr, rhsStr) = this[2].substringAfter('=').trim().split(' ')
    val lhs = lhsStr.toLongOrNull()
    val rhs = rhsStr.toLongOrNull()
    val operator: Long.(Long) -> Long = when (operatorStr) {
        "*" -> Long::times
        "+" -> Long::plus
        else -> error("Operator $operatorStr is not supported!")
    }

    return { initialWorryLevel ->
        val left = (lhs ?: initialWorryLevel) % commonDivisor
        val right = (rhs ?: initialWorryLevel) % commonDivisor

        if (withStressRelief) {
            left.operator(right) / 3
        } else {
            left.operator(right)
        }
    }
}

private fun List<String>.extractMonkeyTargets(): (worryLevel: Long) -> Int {
    val divisor = this[3].substringAfter("by ").toInt()
    val monkeyA = this[4].substringAfter("monkey ").toInt()
    val monkeyB = this[5].substringAfter("monkey ").toInt()

    return { worryLevel -> if (worryLevel % divisor == 0L) monkeyA else monkeyB }
}

private fun List<Monkey>.inspectionsAfter(rounds: Int): List<Long> {
    val monkeys = this.toMutableList()
    val inspections = MutableList(monkeys.size) { 0L }

    repeat(rounds) {
        for (index in monkeys.indices) {
            val monkey = monkeys[index]

            inspections[index] += monkey.items.size.toLong()

            while (monkey.items.isNotEmpty()) {
                val item = monkey.items.removeFirst()

                val updatedWorryLevel = monkey.operation(item)
                val targetMonkey = monkey.nextMonkey(updatedWorryLevel)

                monkeys[targetMonkey].items += updatedWorryLevel
            }
        }
    }

    return inspections
}

private fun List<Long>.toMonkeyBusiness(): Long =
    this.sortedDescending().take(2).reduce { lhs, rhs -> lhs * rhs }

private fun part1(input: List<String>): Long {
    val monkeys = input.toMonkeys(withStressRelief = true)
    val inspections = monkeys.inspectionsAfter(rounds = 20)

    return inspections.toMonkeyBusiness()
}

private fun part2(input: List<String>): Long {
    val monkeys = input.toMonkeys(withStressRelief = false)
    val inspections = monkeys.inspectionsAfter(rounds = 10_000)

    return inspections.toMonkeyBusiness()
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day11_test")
    check(part1(testInput) == 10605L)
    check(part2(testInput) == 2713310158L)

    val input = readInput("Day11")
    println(part1(input))
    println(part2(input))
}
