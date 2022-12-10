private fun computeRegisterValues(input: List<String>) = sequence {
    var registerValue = 1

    for (instruction in input) {
        val instructionParts = instruction.split(' ')
        val op = instructionParts.first()
        val args = instructionParts.drop(1)

        when (op) {
            "noop" -> yield(registerValue)
            "addx" -> {
                yield(registerValue)
                yield(registerValue)
                registerValue += args.single().toInt()
            }
        }
    }
}

private fun part1(input: List<String>): Int {
    val register = computeRegisterValues(input)

    val signalStrengths = register.mapIndexedNotNull { index, registerValue ->
        val cycle = index + 1

        if ((cycle - 20) % 40 == 0) {
            cycle * registerValue
        } else null
    }

    return signalStrengths.sum()
}

private fun part2(input: List<String>): String {
    val register = computeRegisterValues(input)

    val display = register
        .mapIndexed { index, registerValue ->
            val crtPosition = index % 40

            when {
                registerValue - crtPosition in -1..1 -> '#'
                else -> '.'
            }
        }
        .windowed(size = 40, step = 40) { characters -> String(characters.toCharArray()) }

    return display.joinToString(separator = "\n")
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 13140)
    check(part2(testInput) == """
        ##..##..##..##..##..##..##..##..##..##..
        ###...###...###...###...###...###...###.
        ####....####....####....####....####....
        #####.....#####.....#####.....#####.....
        ######......######......######......####
        #######.......#######.......#######.....
    """.trimIndent())

    val input = readInput("Day10")
    println(part1(input))
    println(part2(input))
}
