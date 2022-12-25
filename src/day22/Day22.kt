package day22

import day22.Direction.*
import readInput
import readTestInput
import kotlin.math.sqrt

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

private sealed interface Position {
    val x: Int
    val y: Int
}

data class PositionOnPlane(override val x: Int, override val y: Int) : Position
data class PositionOnCube(override val x: Int, override val y: Int, val cubeFace: Int) : Position
private data class Block<Pos : Position>(val position: Pos, val isWall: Boolean)

private data class Row<Pos : Position>(val blocks: List<Block<Pos>>)
private data class Column<Pos : Position>(val blocks: List<Block<Pos>>)

private data class PlaneCryptoMap(
    val rows: List<Row<PositionOnPlane>>,
    val columns: List<Column<PositionOnPlane>>,
)

private data class CubeFace(
    val rows: List<Row<PositionOnCube>>,
    val columns: List<Column<PositionOnCube>>,

    val neighbors: Map<Direction, Pair<Int, Direction>>
)

private data class CubeCryptoMap(
    val faces: List<CubeFace>,
)

private fun List<String>.toPlaneCryptoMap(): PlaneCryptoMap {
    val rawMapData = this

    val rows: MutableList<MutableList<Block<PositionOnPlane>>> = mutableListOf()
    val columns: MutableList<MutableList<Block<PositionOnPlane>>> = mutableListOf()

    rawMapData.forEachIndexed { y, rawRow ->
        rawRow.forEachIndexed rowLoop@{ x, cell ->
            val isWall = when (cell) {
                '.' -> false
                '#' -> true
                else -> return@rowLoop
            }

            val block = Block(position = PositionOnPlane(x = x, y = y), isWall = isWall)

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

    return PlaneCryptoMap(
        rows = rows.map { rowBlocks -> Row(rowBlocks) },
        columns = columns.map { columnBlocks -> Column(columnBlocks) }
    )
}

private fun List<String>.toCubeCryptoMap(isTestSet: Boolean): CubeCryptoMap {
    val area = this.sumOf { line ->
        line.count { cell -> cell in setOf('.', '#') }
    }
    val faceLength = sqrt(1.0 * area / 6).toInt()

    val faces = if (isTestSet) {
        listOf(
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 2, verticalOffset = 0, cubeFace = 0,
                neighbors = mapOf(LEFT to (2 to DOWN), RIGHT to (5 to LEFT), UP to (1 to DOWN), DOWN to (3 to DOWN)),
            ),
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 0, verticalOffset = 1, cubeFace = 1,
                neighbors = mapOf(LEFT to (5 to UP), RIGHT to (2 to RIGHT), UP to (0 to DOWN), DOWN to (4 to UP)),
            ),
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 1, verticalOffset = 1, cubeFace = 2,
                neighbors = mapOf(LEFT to (1 to LEFT), RIGHT to (3 to RIGHT), UP to (0 to RIGHT), DOWN to (4 to RIGHT)),
            ),
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 2, verticalOffset = 1, cubeFace = 3,
                neighbors = mapOf(LEFT to (2 to LEFT), RIGHT to (5 to DOWN), UP to (0 to UP), DOWN to (4 to DOWN)),
            ),
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 2, verticalOffset = 2, cubeFace = 4,
                neighbors = mapOf(LEFT to (2 to UP), RIGHT to (5 to RIGHT), UP to (3 to UP), DOWN to (1 to UP)),
            ),
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 3, verticalOffset = 2, cubeFace = 5,
                neighbors = mapOf(LEFT to (4 to LEFT), RIGHT to (0 to LEFT), UP to (3 to LEFT), DOWN to (1 to RIGHT)),
            ),
        )
    } else {
        listOf(
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 1, verticalOffset = 0, cubeFace = 0,
                neighbors = mapOf(LEFT to (3 to RIGHT), RIGHT to (1 to RIGHT), UP to (5 to RIGHT), DOWN to (2 to DOWN)),
            ),
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 2, verticalOffset = 0, cubeFace = 1,
                neighbors = mapOf(LEFT to (0 to LEFT), RIGHT to (4 to LEFT), UP to (5 to UP), DOWN to (2 to LEFT)),
            ),
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 1, verticalOffset = 1, cubeFace = 2,
                neighbors = mapOf(LEFT to (3 to DOWN), RIGHT to (1 to UP), UP to (0 to UP), DOWN to (4 to DOWN)),
            ),
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 0, verticalOffset = 2, cubeFace = 3,
                neighbors = mapOf(LEFT to (0 to RIGHT), RIGHT to (4 to RIGHT), UP to (2 to RIGHT), DOWN to (5 to DOWN)),
            ),
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 1, verticalOffset = 2, cubeFace = 4,
                neighbors = mapOf(LEFT to (3 to LEFT), RIGHT to (1 to LEFT), UP to (2 to UP), DOWN to (5 to LEFT)),
            ),
            extractCubeFace(
                faceLength = faceLength, horizontalOffset = 0, verticalOffset = 3, cubeFace = 5,
                neighbors = mapOf(LEFT to (0 to DOWN), RIGHT to (4 to UP), UP to (3 to UP), DOWN to (1 to DOWN)),
            ),
        )
    }

    return CubeCryptoMap(faces)
}

private fun List<String>.extractCubeFace(
    cubeFace: Int,
    faceLength: Int,
    horizontalOffset: Int,
    verticalOffset: Int,
    neighbors: Map<Direction, Pair<Int, Direction>>,
): CubeFace {
    val rawMapData = this

    val rawFaceData = rawMapData
        .drop(verticalOffset * faceLength)
        .take(faceLength)
        .map { mapRow -> mapRow.drop(horizontalOffset * faceLength).take(faceLength) }

    val rows: MutableList<MutableList<Block<PositionOnCube>>> = mutableListOf()
    val columns: MutableList<MutableList<Block<PositionOnCube>>> = mutableListOf()

    rawFaceData.forEachIndexed { y, rawRow ->
        rawRow.forEachIndexed rowLoop@{ x, cell ->
            val isWall = when (cell) {
                '.' -> false
                '#' -> true
                else -> return@rowLoop
            }

            val position = PositionOnCube(
                x = x + horizontalOffset * faceLength,
                y = y + verticalOffset * faceLength,
                cubeFace = cubeFace
            )
            val block = Block(position = position, isWall = isWall)

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

    return CubeFace(
        rows = rows.map { rowBlocks -> Row(rowBlocks) },
        columns = columns.map { columnBlocks -> Column(columnBlocks) },
        neighbors = neighbors,
    )
}


private fun PlaneCryptoMap.findStartPosition(): Position {
    val topRow = rows.first()
    val startingBlock = topRow.blocks.first()

    return startingBlock.position
}

private fun CubeCryptoMap.findStartPosition(): Position {
    val firstFace = faces.first()
    val topRow = firstFace.rows.first()
    val startingBlock = topRow.blocks.first()

    return startingBlock.position
}

private enum class Direction { LEFT, RIGHT, UP, DOWN }
private data class Actor(val position: Position, val direction: Direction)

private fun Actor.execute(instruction: Instruction, on: PlaneCryptoMap): Actor {
    return when (instruction) {
        is Instruction.Move -> {
            val updatedPosition = when (direction) {
                LEFT -> {
                    val row = on.rows[position.y]

                    moveBackward(steps = instruction.steps, blocks = row.blocks)
                }

                RIGHT -> {
                    val row = on.rows[position.y]

                    moveForward(steps = instruction.steps, blocks = row.blocks)
                }

                UP -> {
                    val column = on.columns[position.x]

                    moveBackward(steps = instruction.steps, blocks = column.blocks)
                }

                DOWN -> {
                    val column = on.columns[position.x]

                    moveForward(steps = instruction.steps, blocks = column.blocks)
                }
            }

            copy(position = updatedPosition)
        }

        Instruction.TurnLeft -> {
            val updatedDirection = when (direction) {
                LEFT -> DOWN
                RIGHT -> UP
                UP -> LEFT
                DOWN -> RIGHT
            }

            copy(direction = updatedDirection)
        }

        Instruction.TurnRight -> {
            val updatedDirection = when (direction) {
                LEFT -> UP
                RIGHT -> DOWN
                UP -> RIGHT
                DOWN -> LEFT
            }

            copy(direction = updatedDirection)
        }
    }
}

private fun Actor.moveForward(steps: Int, blocks: List<Block<PositionOnPlane>>): Position {
    val startIndex = blocks.indexOfFirst { it.position == position }

    var targetPosition = position

    for (index in (startIndex + 1)..(startIndex + steps)) {
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

private fun Actor.moveBackward(steps: Int, blocks: List<Block<PositionOnPlane>>): Position {
    val startIndex = blocks.indexOfFirst { it.position == position }

    var targetPosition = position

    for (index in (startIndex - 1) downTo (startIndex - steps)) {
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

private fun Actor.execute(instruction: Instruction, on: CubeCryptoMap): Actor {
    return when (instruction) {
        is Instruction.Move -> {
            move(steps = instruction.steps, on = on)
        }

        Instruction.TurnLeft -> {
            val updatedDirection = when (direction) {
                LEFT -> DOWN
                RIGHT -> UP
                UP -> LEFT
                DOWN -> RIGHT
            }

            copy(direction = updatedDirection)
        }

        Instruction.TurnRight -> {
            val updatedDirection = when (direction) {
                LEFT -> UP
                RIGHT -> DOWN
                UP -> RIGHT
                DOWN -> LEFT
            }

            copy(direction = updatedDirection)
        }
    }
}

private fun Actor.move(steps: Int, on: CubeCryptoMap): Actor {
    var stepsRemaining = steps

    var position = position as PositionOnCube
    var direction = direction

    while (stepsRemaining > 0) {
        val (nextBlock, nextDirection) = when (direction) {
            LEFT -> moveLeft(on = on, from = position, into = direction)
            RIGHT -> moveRight(on = on, from = position, into = direction)
            UP -> moveUp(on = on, from = position, into = direction)
            DOWN -> moveDown(on = on, from = position, into = direction)
        }

        if (nextBlock.isWall) {
            break
        }

        position = nextBlock.position
        direction = nextDirection

        stepsRemaining -= 1
    }

    return Actor(position, direction)
}

private fun moveLeft(
    on: CubeCryptoMap,
    from: PositionOnCube,
    into: Direction
): Pair<Block<PositionOnCube>, Direction> {
    val faceLength = on.faces.first().rows.size

    val faceRow = on.faces[from.cubeFace].rows[from.y % faceLength]
    val nextX = (from.x % faceLength) - 1

    return if (nextX in faceRow.blocks.indices) {
        faceRow.blocks[nextX] to into
    } else {
        val sourceCubeFace = on.faces[from.cubeFace]
        val (targetCubeFaceNumber, targetDirection) = sourceCubeFace.neighbors.getValue(LEFT)

        val targetCubeFace = on.faces[targetCubeFaceNumber]
        val targetPosition = when(targetDirection) {
            LEFT -> targetCubeFace.columns.last().blocks[from.y % faceLength]
            RIGHT -> targetCubeFace.columns.first().blocks.asReversed()[from.y % faceLength]
            UP -> targetCubeFace.rows.last().blocks.asReversed()[from.y % faceLength]
            DOWN -> targetCubeFace.rows.first().blocks[from.y % faceLength]
        }

        targetPosition to targetDirection
    }
}

private fun moveRight(
    on: CubeCryptoMap,
    from: PositionOnCube,
    into: Direction
): Pair<Block<PositionOnCube>, Direction> {
    val faceLength = on.faces.first().rows.size

    val faceRow = on.faces[from.cubeFace].rows[from.y % faceLength]
    val nextX = (from.x % faceLength) + 1

    return if (nextX in faceRow.blocks.indices) {
        faceRow.blocks[nextX] to into
    } else {
        val sourceCubeFace = on.faces[from.cubeFace]
        val (targetCubeFaceNumber, targetDirection) = sourceCubeFace.neighbors.getValue(RIGHT)

        val targetCubeFace = on.faces[targetCubeFaceNumber]
        val targetPosition = when(targetDirection) {
            LEFT -> targetCubeFace.columns.last().blocks.asReversed()[from.y % faceLength]
            RIGHT -> targetCubeFace.columns.first().blocks[from.y % faceLength]
            UP -> targetCubeFace.rows.last().blocks[from.y % faceLength]
            DOWN -> targetCubeFace.rows.first().blocks.asReversed()[from.y % faceLength]
        }

        targetPosition to targetDirection
    }
}

private fun moveUp(
    on: CubeCryptoMap,
    from: PositionOnCube,
    into: Direction
): Pair<Block<PositionOnCube>, Direction> {
    val faceLength = on.faces.first().columns.size

    val faceColumn = on.faces[from.cubeFace].columns[from.x % faceLength]
    val nextY = (from.y % faceLength) - 1

    return if (nextY in faceColumn.blocks.indices) {
        faceColumn.blocks[nextY] to into
    } else {
        val sourceCubeFace = on.faces[from.cubeFace]
        val (targetCubeFaceNumber, targetDirection) = sourceCubeFace.neighbors.getValue(UP)

        val targetCubeFace = on.faces[targetCubeFaceNumber]
        val targetPosition = when(targetDirection) {
            LEFT -> targetCubeFace.columns.last().blocks.asReversed()[from.x % faceLength]
            RIGHT -> targetCubeFace.columns.first().blocks[from.x % faceLength]
            UP -> targetCubeFace.rows.last().blocks[from.x % faceLength]
            DOWN -> targetCubeFace.rows.first().blocks.asReversed()[from.x % faceLength]
        }

        targetPosition to targetDirection
    }
}

private fun moveDown(
    on: CubeCryptoMap,
    from: PositionOnCube,
    into: Direction
): Pair<Block<PositionOnCube>, Direction> {
    val faceLength = on.faces.first().columns.size

    val faceColumn = on.faces[from.cubeFace].columns[from.x % faceLength]
    val nextY = (from.y % faceLength) + 1

    return if (nextY in faceColumn.blocks.indices) {
        faceColumn.blocks[nextY] to into
    } else {
        val sourceCubeFace = on.faces[from.cubeFace]
        val (targetCubeFaceNumber, targetDirection) = sourceCubeFace.neighbors.getValue(DOWN)

        val targetCubeFace = on.faces[targetCubeFaceNumber]
        val targetPosition = when(targetDirection) {
            LEFT -> targetCubeFace.columns.last().blocks[from.x % faceLength]
            RIGHT -> targetCubeFace.columns.first().blocks.asReversed()[from.x % faceLength]
            UP -> targetCubeFace.rows.last().blocks.asReversed()[from.x % faceLength]
            DOWN -> targetCubeFace.rows.first().blocks[from.x % faceLength]
        }

        targetPosition to targetDirection
    }
}


private fun Actor.calculatePassword(): Int {
    val row = position.y + 1
    val column = position.x + 1

    val facing = when (direction) {
        LEFT -> 2
        RIGHT -> 0
        UP -> 3
        DOWN -> 1
    }

    return 1_000 * row + 4 * column + facing
}

private fun List<String>.parseAsPlainMap(): Pair<PlaneCryptoMap, Instructions> {
    val rawInput = this

    val cryptoMap = rawInput.dropLast(2).toPlaneCryptoMap()
    val instructions = rawInput.last().toInstructions()

    return cryptoMap to instructions
}

private fun List<String>.parseAsCubeMap(isTestSet: Boolean): Pair<CubeCryptoMap, Instructions> {
    val rawInput = this

    val cryptoMap = rawInput.dropLast(2).toCubeCryptoMap(isTestSet)
    val instructions = rawInput.last().toInstructions()

    return cryptoMap to instructions
}

private fun part1(input: List<String>): Int {
    val (cryptoMap, instructions) = input.parseAsPlainMap()
    val startPosition = cryptoMap.findStartPosition()
    var actor = Actor(startPosition, RIGHT)

    for (instruction in instructions) {
        actor = actor.execute(instruction = instruction, on = cryptoMap)
    }

    return actor.calculatePassword()
}

private fun part2(input: List<String>, isTestSet: Boolean): Int {
    val (cryptoMap, instructions) = input.parseAsCubeMap(isTestSet = isTestSet)

    val startPosition = cryptoMap.findStartPosition()
    var actor = Actor(startPosition, RIGHT)

    for (instruction in instructions) {
        actor = actor.execute(instruction = instruction, on = cryptoMap)
    }

    return actor.calculatePassword()
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day22")
    check(part1(testInput) == 6032)
    check(part2(testInput, isTestSet = true) == 5031)

    val input = readInput("Day22")
    println(part1(input))
    println(part2(input, isTestSet = false))
}
