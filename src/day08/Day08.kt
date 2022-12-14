package day08

import readInput
import readTestInput

private data class Tree(
    val x: Int,
    val y: Int,
    val height: Int,
)

private fun List<String>.parse(): List<List<Tree>> {
    return this.mapIndexed { y, row ->
        row.mapIndexed { x, cell ->
            Tree(x = x, y = y, height = cell.digitToInt())
        }
    }
}

private fun List<Tree>.filterIsVisible(): List<Tree> {
    var maxHeight = -1

    return this.filter { tree ->
        if (tree.height > maxHeight) {
            maxHeight = tree.height

            true
        } else false
    }
}

private fun List<Tree>.countVisibleFrom(baseTree: Tree): Int =
    (indexOfFirst { tree -> tree.height >= baseTree.height }.takeUnless { it < 0 } ?: lastIndex) + 1

private fun part1(input: List<String>): Int {
    val treeRows = input.parse()
    val treeColumns = List(treeRows.size) { column ->
        treeRows[column].indices.map { row ->
            treeRows[row][column]
        }
    }

    val visibleRowTrees =
        treeRows.flatMap { treeRow -> treeRow.filterIsVisible() + treeRow.asReversed().filterIsVisible() }
    val visibleColumnTrees =
        treeColumns.flatMap { column -> column.filterIsVisible() + column.asReversed().filterIsVisible() }

    val visibleTrees = (visibleRowTrees + visibleColumnTrees).toSet()

    return visibleTrees.size
}

private fun part2(input: List<String>): Int {
    val treeRows = input.parse()
    val treeColumns = List(treeRows.size) { column ->
        treeRows[column].indices.map { row ->
            treeRows[row][column]
        }
    }

    val highestScenicScore = treeRows.indices.maxOf { x ->
        treeColumns.indices.maxOf { y ->
            val baseTree = treeColumns[x][y]

            val treesToTheLeft = treeRows[y].take(x).asReversed()
            val treesToTheRight = treeRows[y].drop(x + 1)
            val treesAbove = treeColumns[x].take(y).asReversed()
            val treesBelow = treeColumns[x].drop(y + 1)

            val viewLeft = treesToTheLeft.countVisibleFrom(baseTree)
            val viewRight = treesToTheRight.countVisibleFrom(baseTree)
            val viewAbove = treesAbove.countVisibleFrom(baseTree)
            val viewBelow = treesBelow.countVisibleFrom(baseTree)

            viewLeft * viewRight * viewAbove * viewBelow
        }
    }

    return highestScenicScore
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day08")
    check(part1(testInput) == 21)
    check(part2(testInput) == 8)

    val input = readInput("Day08")
    println(part1(input))
    println(part2(input))
}
