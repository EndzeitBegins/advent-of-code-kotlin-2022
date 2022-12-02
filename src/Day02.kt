private fun String.toShape(): Shape = when (this) {
    "A", "X" -> Shape.Rock
    "B", "Y" -> Shape.Paper
    "C", "Z" -> Shape.Scissor
    else -> error(""""$this" is NOT a legal shape!""")
}

private fun String.toResult(): DuelResult = when (this) {
    "X" -> DuelResult.Lose
    "Y" -> DuelResult.Draw
    "Z" -> DuelResult.Won
    else -> error(""""$this" is NOT a legal result!""")
}

private sealed interface Shape {
    val score: Int
    infix fun against(other: Shape): DuelResult

    object Rock : Shape {
        override val score: Int = 1

        override fun against(other: Shape): DuelResult = when (other) {
            Paper -> DuelResult.Lose
            Rock -> DuelResult.Draw
            Scissor -> DuelResult.Won
        }
    }

    object Paper : Shape {
        override val score: Int = 2

        override fun against(other: Shape): DuelResult = when (other) {
            Paper -> DuelResult.Draw
            Rock -> DuelResult.Won
            Scissor -> DuelResult.Lose
        }
    }

    object Scissor : Shape {
        override val score: Int = 3

        override fun against(other: Shape): DuelResult = when (other) {
            Paper -> DuelResult.Won
            Rock -> DuelResult.Lose
            Scissor -> DuelResult.Draw
        }
    }
}

private sealed interface DuelResult {
    val score: Int

    object Won : DuelResult {
        override val score: Int = 6
    }

    object Lose : DuelResult {
        override val score: Int = 0
    }

    object Draw : DuelResult {
        override val score: Int = 3
    }
}

private fun part1(input: List<String>): Int {
    return input.sumOf { game ->
        val (opponent, me) = game.split(" ")

        val opponentShape = opponent.toShape()
        val selectedShape = me.toShape()

        selectedShape.score + (selectedShape against opponentShape).score
    }
}

private fun part2(input: List<String>): Int {
    return input.sumOf { game ->
        val (opponent, me) = game.split(" ")

        val opponentShape = opponent.toShape()
        val desiredResult = me.toResult()
        val selectedShape = Shape::class.sealedSubclasses
            .mapNotNull { it.objectInstance }
            .first { shape -> shape against opponentShape == desiredResult }

        selectedShape.score + desiredResult.score
    }
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 12)

    val input = readInput("Day02")
    println(part1(input))
    println(part2(input))
}
