package day21

import readInput
import readTestInput

private sealed interface MonkeyTask {

    class SolvedTask(val solution: Long) : MonkeyTask

    class OpenTask(
        val lhs: String,
        val rhs: String,
        val operation: Char
    ) : MonkeyTask {
        fun solve(lhsValue: Long, rhsValue: Long): SolvedTask = SolvedTask(
            solution = when (operation) {
                '+' -> lhsValue + rhsValue
                '-' -> lhsValue - rhsValue
                '*' -> lhsValue * rhsValue
                '/' -> lhsValue / rhsValue
                else -> error("""Unsupported operation "$operation"!""")
            }
        )
    }
}

private fun MonkeyTask.solutionOrNull(): Long? = when (this) {
    is MonkeyTask.OpenTask -> null
    is MonkeyTask.SolvedTask -> solution
}

private fun List<String>.toMonkeyTasks(): Map<String, MonkeyTask> =
    associate { line ->
        val (name, task) = line.split(": ")
        val monkeyTask = task.toMonkeyTask()

        name to monkeyTask
    }

private fun Map<String, MonkeyTask>.solveTask(monkeyName: String): MonkeyTask.SolvedTask {
    val solvedTasks = this.solveAllSolvableTasks()

    return solvedTasks.getValue(monkeyName) as MonkeyTask.SolvedTask
}

private val Map<String, MonkeyTask>.solvedTasks: Int
    get() = count { (_, monkeyTask) -> monkeyTask is MonkeyTask.SolvedTask }

private val monkeyTaskRegex = """(\w+) ([*/+-]) (\w+)""".toRegex()

private fun String.toMonkeyTask(): MonkeyTask {
    val regexMatch = monkeyTaskRegex.matchEntire(this)

    return if (regexMatch == null) {
        MonkeyTask.SolvedTask(this.toLong())
    } else {
        val (lhs, operation, rhs) = regexMatch.destructured

        MonkeyTask.OpenTask(
            lhs = lhs,
            rhs = rhs,
            operation = operation.single()
        )
    }
}

private fun Map<String, MonkeyTask>.solveAllSolvableTasks(): Map<String, MonkeyTask> {
    var ongoingMonkeyTasks = this

    var previouslySolvedTasks = 0
    var solvedTasks = this.solvedTasks

    while (solvedTasks > previouslySolvedTasks) {
        ongoingMonkeyTasks = ongoingMonkeyTasks.mapValues { (_, monkeyTask) ->
            if (monkeyTask is MonkeyTask.OpenTask) {
                val lhs = ongoingMonkeyTasks[monkeyTask.lhs]?.solutionOrNull()
                val rhs = ongoingMonkeyTasks[monkeyTask.rhs]?.solutionOrNull()

                if (lhs != null && rhs != null) {
                    return@mapValues monkeyTask.solve(lhs, rhs)
                }
            }

            return@mapValues monkeyTask
        }

        previouslySolvedTasks = solvedTasks
        solvedTasks = ongoingMonkeyTasks.solvedTasks
    }

    return ongoingMonkeyTasks
}

private fun Map<String, MonkeyTask>.reverseSolve(
    implicitlyKnownMonkeyTask: String,
    equationValue: Long,
): Map<String, MonkeyTask> {
    val monkeyTasks = this
    val solutionsToApply = mutableListOf(
        implicitlyKnownMonkeyTask to equationValue
    )
    val knownSolutions = mutableMapOf<String, Long>()

    while (solutionsToApply.isNotEmpty()) {
        val (knownMonkeyName, knownSolution) = solutionsToApply.removeFirst()

        for ((monkeyName, monkeyTask) in monkeyTasks) {
            if (monkeyName == knownMonkeyName && monkeyTask is MonkeyTask.OpenTask) {
                val lhsValue = monkeyTasks[monkeyTask.lhs]?.solutionOrNull() ?: knownSolutions[monkeyTask.lhs]
                val rhsValue = monkeyTasks[monkeyTask.rhs]?.solutionOrNull() ?: knownSolutions[monkeyTask.rhs]

                if (lhsValue == null) {
                    val solved = when (monkeyTask.operation) {
                        '+' -> knownSolution - checkNotNull(rhsValue)
                        '-' -> knownSolution + checkNotNull(rhsValue)
                        '*' -> knownSolution / checkNotNull(rhsValue)
                        '/' -> knownSolution * checkNotNull(rhsValue)
                        else -> error("oopsie!")
                    }

                    solutionsToApply += (monkeyTask.lhs to solved)
                }
                if (rhsValue == null) {
                    val solved = when (monkeyTask.operation) {
                        '+' -> knownSolution - checkNotNull(lhsValue)
                        '-' -> -1 * (knownSolution - checkNotNull(lhsValue))
                        '*' -> knownSolution / checkNotNull(lhsValue)
                        '/' -> checkNotNull(lhsValue) / knownSolution
                        else -> error("oopsie!")
                    }

                    solutionsToApply += (monkeyTask.rhs to solved)
                }
            }
        }

        knownSolutions[knownMonkeyName] = knownSolution
    }

    return monkeyTasks + knownSolutions
        .mapValues { (monkeyName, solution) -> MonkeyTask.SolvedTask(solution) }
}

private fun part1(input: List<String>): Long {
    val monkeyTasks = input.toMonkeyTasks()
    val solvedRootTask = monkeyTasks.solveTask("root")

    return solvedRootTask.solution
}

private fun part2(input: List<String>): Long {
    val monkeyTasks = input.toMonkeyTasks()

    val rootTask = monkeyTasks.getValue("root") as MonkeyTask.OpenTask
    val rootLhs = rootTask.lhs
    val rootRhs = rootTask.rhs

    val relevantTasks = monkeyTasks
        .filterNot { (monkeyName, _) -> monkeyName == "humn" }
    val solvedRelevantTasks = relevantTasks.solveAllSolvableTasks()

    val rootLhsTask = solvedRelevantTasks.getValue(rootLhs)
    val rootRhsTask = solvedRelevantTasks.getValue(rootRhs)

    val (implicitlyKnownMonkeyTask, equationValue) = when {
        rootLhsTask is MonkeyTask.SolvedTask -> rootRhs to rootLhsTask.solution
        rootRhsTask is MonkeyTask.SolvedTask -> rootLhs to rootRhsTask.solution
        else -> error("Cannot solve with equation, when neither lhs nor rhs are known!")
    }

    val solvedTasks = solvedRelevantTasks.reverseSolve(implicitlyKnownMonkeyTask, equationValue)

    return checkNotNull(solvedTasks.getValue("humn").solutionOrNull())
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day21")
    check(part1(testInput) == 152L)
    check(part2(testInput) == 301L)

    val input = readInput("Day21")
    println(part1(input))
    println(part2(input))
}
