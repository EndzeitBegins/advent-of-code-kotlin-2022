package day22

import readInput
import readTestInput

private lateinit var printedMap: MutableList<MutableList<Char>>

private sealed interface Instruction {
    data class Move(val steps: Int) : Instruction
    object TurnLeft : Instruction
    object TurnRight : Instruction
}
private typealias Instructions = Sequence<Instruction>

private fun String.toInstructions(): Instructions {
    val rawInstructions = this

    return sequence {
        var digitBuffer = ""

        for (rawInstruction in rawInstructions) {
            if (rawInstruction in '0'..'9') {
                digitBuffer += rawInstruction
            } else {
                if (digitBuffer.isNotEmpty()) {
                    yield(
                        Instruction.Move(steps = digitBuffer.toInt())
                    )
                    digitBuffer = ""
                }
                val instruction = if (rawInstruction == 'L') Instruction.TurnLeft else Instruction.TurnRight
                yield(instruction)
            }
        }

        if (digitBuffer.isNotEmpty()) {
            yield(
                Instruction.Move(steps = digitBuffer.toInt())
            )
        }
    }
}

private data class Position(val x: Int, val y: Int)
private data class Block(val position: Position, val isWall: Boolean)

private data class Row(val blocks: List<Block>) {
    val size = blocks.size

    val minX = blocks.first().position.x
    val maxX = blocks.last().position.x
}

private data class Column(val blocks: List<Block>) {
    val minY = blocks.first().position.y
    val maxY = blocks.last().position.y
}

private data class CryptoMap(
    val rows: List<Row>,
    val columns: List<Column>,
)

private fun List<String>.toCryptoMap(): CryptoMap {
    val rawMapData = this

    val rows: MutableList<MutableList<Block>> = mutableListOf()
    val columns: MutableList<MutableList<Block>> = mutableListOf()

    rawMapData.forEachIndexed { y, rawRow ->
        rawRow.forEachIndexed rowLoop@{ x, cell ->
            val isWall = when (cell) {
                '.' -> false
                '#' -> true
                else -> return@rowLoop
            }

            val block = Block(position = Position(x = x, y = y), isWall = isWall)

            repeat(y - rows.lastIndex) {
                rows.add(mutableListOf())
            }
            rows[y] += block

            repeat(x - columns.lastIndex) {
                columns.add(mutableListOf())
            }
            columns[x] += block
        }
    }

    return CryptoMap(
        rows = rows.map { rowBlocks -> Row(rowBlocks) },
        columns = columns.map { columnBlocks -> Column(columnBlocks) }
    )
}

private fun CryptoMap.findStartPosition(): Position {
    val topRow = rows.first()
    val startingBlock = topRow.blocks.first()

    return startingBlock.position
}

private enum class Direction { LEFT, RIGHT, UP, DOWN }
private data class Actor(val position: Position, val direction: Direction)

private fun Actor.execute(instruction: Instruction, on: CryptoMap): Actor {
    return when (instruction) {
        is Instruction.Move -> {
            val updatedPosition = when (direction) {
                Direction.LEFT -> {
                    val row = on.rows[position.y]

                    moveBackward(steps = instruction.steps, blocks = row.blocks)
                }

                Direction.RIGHT -> {
                    val row = on.rows[position.y]

                    moveForward(steps = instruction.steps, blocks = row.blocks)
                }

                Direction.UP -> {
                    val column = on.columns[position.x]

                    moveBackward(steps = instruction.steps, blocks = column.blocks)
                }

                Direction.DOWN -> {
                    val column = on.columns[position.x]

                    moveForward(steps = instruction.steps, blocks = column.blocks)
                }
            }

            copy(position = updatedPosition)
        }

        Instruction.TurnLeft -> {
            val updatedDirection = when (direction) {
                Direction.LEFT -> Direction.DOWN
                Direction.RIGHT -> Direction.UP
                Direction.UP -> Direction.LEFT
                Direction.DOWN -> Direction.RIGHT
            }

            copy(direction = updatedDirection)
        }

        Instruction.TurnRight -> {
            val updatedDirection = when (direction) {
                Direction.LEFT -> Direction.UP
                Direction.RIGHT -> Direction.DOWN
                Direction.UP -> Direction.RIGHT
                Direction.DOWN -> Direction.LEFT
            }

            copy(direction = updatedDirection)
        }
    }
}

private fun Actor.moveForward(steps: Int, blocks: List<Block>): Position {
    val startIndex = blocks.indexOfFirst { it.position == position }

    var targetPosition = position

    for (index in (startIndex + 1)..(startIndex + steps)) {

        // TODO remove ---
        printedMap[targetPosition.y][targetPosition.x] = when(direction) {
            Direction.LEFT -> '<'
            Direction.RIGHT -> '>'
            Direction.UP -> 'A'
            Direction.DOWN -> 'v'
        }
        // TODO remove ---

        val sanitizedIndex = index.mod(blocks.size)

        val potentialTargetPosition = blocks[sanitizedIndex]

        if (!potentialTargetPosition.isWall) {
            targetPosition = potentialTargetPosition.position
        } else {
            break
        }
    }

    return targetPosition
}

private fun Actor.moveBackward(steps: Int, blocks: List<Block>): Position {
    val startIndex = blocks.indexOfFirst { it.position == position }

    var targetPosition = position

    for (index in (startIndex - 1) downTo (startIndex - steps)) {

        // TODO remove ---
        printedMap[targetPosition.y][targetPosition.x] = when(direction) {
            Direction.LEFT -> '<'
            Direction.RIGHT -> '>'
            Direction.UP -> '^'
            Direction.DOWN -> 'v'
        }
        // TODO remove ---


        val sanitizedIndex = index.mod(blocks.size)

        val potentialTargetPosition = blocks[sanitizedIndex]

        if (!potentialTargetPosition.isWall) {
            targetPosition = potentialTargetPosition.position
        } else {
            break
        }
    }

    return targetPosition
}

private fun Actor.calculatePassword(): Int {
    val row = position.y + 1
    val column = position.x + 1

    val facing = when (direction) {
        Direction.LEFT -> 2
        Direction.RIGHT -> 0
        Direction.UP -> 3
        Direction.DOWN -> 1
    }

    return 1_000 * row + 4 * column + facing
}

private fun List<String>.parse(): Pair<CryptoMap, Instructions> {
    val rawInput = this

    val cryptoMap = rawInput.dropLast(2).toCryptoMap()
    val instructions = rawInput.last().toInstructions()

    return cryptoMap to instructions
}

private fun part1(input: List<String>): Int {
    val (cryptoMap, instructions) = input.parse()


    // TODO remove ---
    printedMap = MutableList(cryptoMap.rows.size) {
        MutableList(cryptoMap.columns.size) { ' ' }
    }
    for (block in (cryptoMap.rows.flatMap { it.blocks } + cryptoMap.columns.flatMap { it.blocks })) {
        if (block.isWall) {
            printedMap[block.position.y][block.position.x] = '#'
        }
    }
    // TODO remove ---


    val startPosition = cryptoMap.findStartPosition()
    var actor = Actor(startPosition, Direction.RIGHT)


    println("instrc:")
    println(instructions.joinToString(separator = "") { instruction ->
        when(instruction) {
            is Instruction.Move -> "${instruction.steps}"
            is Instruction.TurnLeft -> "L"
            is Instruction.TurnRight -> "R"
        }
    })

    for (instruction in instructions) {
        actor = actor.execute(instruction = instruction, on = cryptoMap)
    }

    // TODO remove ---
    println(printedMap.joinToString(separator = "\n") { row ->
        row.joinToString(separator = "")
    })
    // TODO remove ---

    return actor.calculatePassword()
}

private fun part2(input: List<String>): Int {
    return input.size
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day22")
//    check(part1(testInput) == 6032)
//    check(part2(testInput) == 1)

    val input = readInput("Day22")
    println(part1(input))
    println(part2(input))
}
