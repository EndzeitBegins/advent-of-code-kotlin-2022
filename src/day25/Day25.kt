package day25

import readInput
import readTestInput
import kotlin.math.pow

private val snafuPlaceValues = mutableListOf<Long>()

private fun snafuToDecimal(snafu: String): Long {
    var value = 0L

    for (exponent in snafu.indices) {
        val index = snafu.lastIndex - exponent

        val digitValue = when(snafu[index]) {
            '0' -> 0
            '1' -> 1
            '2' -> 2
            '-' -> -1
            '=' -> -2
            else -> error("Unsupported digit '${snafu[index]}' for snafu format!")
        }
        
        if (snafuPlaceValues.lastIndex < exponent) {
            snafuPlaceValues.add(5.0.pow(exponent).toLong())
        }

        value += digitValue * snafuPlaceValues[exponent]
    }

    return value
}

private fun decimalToSnafu(decimal: Long): String {
    val quinary = decimal.toString(radix = 5)
    
    val snafu = buildString {
        var remainder: Int? = null

        for (index in quinary.indices.reversed()) {
            val placeValue = quinary[index].digitToInt() + (remainder ?: 0)
            val digit = placeValue % 5
            remainder = placeValue / 5

            val snafuRepresentation = when(digit) {
                0 -> "0"
                1 -> "1"
                2 -> "2"
                3 -> "1="
                4 -> "1-"
                else -> error("Cannot represent $digit as snafu digit!")
            }

            append(snafuRepresentation.last())

            if (snafuRepresentation.length > 1) {
                remainder = snafuToDecimal(snafuRepresentation.dropLast(1)).toInt()
            }
        }
    }.reversed()
    
    return snafu
}

private fun part1(input: List<String>): String {
    val fuelAmount = input
        .sumOf { snafuNumber -> snafuToDecimal(snafuNumber) }

    return decimalToSnafu(fuelAmount)
}

private fun part2(input: List<String>): Int {
    return input.size
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day25")
    check(part1(testInput) == "2=-1=0")
//    check(part2(testInput) == 1)

    val input = readInput("Day25")
    println(part1(input))
    println(part2(input))
}
