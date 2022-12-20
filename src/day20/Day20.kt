package day20

import readInput
import readTestInput

data class DataBuffer(
    val originalIndex: Int,
    val value: Long,
)

private fun List<String>.parseDataBuffers(): List<DataBuffer> =
    mapIndexed { index, line -> DataBuffer(value = line.toLong(), originalIndex = index) }

private fun List<DataBuffer>.mixed(): List<DataBuffer> {
    val dataBuffers = this

    val modulus = dataBuffers.lastIndex
    val mixedDataBuffers = dataBuffers.toMutableList()

    for (index in dataBuffers.indices) {
        val actualIndex = mixedDataBuffers.indexOfFirst { it.originalIndex == index }

        val item = mixedDataBuffers.removeAt(actualIndex)
        val targetIndex = (actualIndex + item.value).mod(modulus)

        mixedDataBuffers.add(targetIndex, item)
    }

    return mixedDataBuffers
}

private fun List<DataBuffer>.calculateCoordinates(): Long {
    val dataBuffers = this

    val startIndex = dataBuffers.indexOfFirst { it.value == 0L }

    return listOf(1_000, 2_000, 3_000)
        .sumOf { offset ->
            val targetIndex = (startIndex + offset).mod(dataBuffers.size)

            dataBuffers[targetIndex].value
        }
}

private fun DataBuffer.decryptWith(decryptionKey: Int): DataBuffer =
    DataBuffer(originalIndex, value * decryptionKey)

private fun List<DataBuffer>.decryptWith(decryptionKey: Int): List<DataBuffer> =
    this.map { it.decryptWith(decryptionKey) }

private fun part1(input: List<String>): Long {
    val dataBuffers = input.parseDataBuffers()

    val mixedBuffers = dataBuffers.mixed()

    return mixedBuffers.calculateCoordinates()
}

private fun part2(input: List<String>): Long {
    val encryptedNumbers = input.parseDataBuffers()
    val decryptedNumbers = encryptedNumbers.decryptWith(decryptionKey = 811589153)

    var mixedNumbers = decryptedNumbers
    repeat(10) {
        mixedNumbers = mixedNumbers.mixed()
    }

    return mixedNumbers.calculateCoordinates()
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day20")
    check(part1(testInput) == 3L)
    check(part2(testInput) == 1623178306L)

    val input = readInput("Day20")
    println(part1(input))
    println(part2(input))
}
