package day13

import day13.Fragment.CollectionFragment
import day13.Fragment.DataFragment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import readInput
import readTestInput

private data class Packet (
    val fragments: CollectionFragment,
): Comparable<Packet> {

    override fun compareTo(other: Packet): Int {
        return this.fragments.compareTo(other.fragments)
    }
}

private fun List<String>.toPacketPairs(): List<Pair<Packet, Packet>> {
    return this.windowed(size = 2, step = 3) { (leftRawPacket, rightRawPacket) ->
        leftRawPacket.toPacket() to rightRawPacket.toPacket()
    }
}

private sealed interface Fragment {
    data class DataFragment(val value: Int) : Fragment
    data class CollectionFragment(val values: List<Fragment>) : Fragment
}

private fun String.toPacket(): Packet {
    val parsedPacket: JsonArray = Json.decodeFromString(this)

    return Packet(parsedPacket.toCollectionFragment())
}

private fun JsonArray.toCollectionFragment(): CollectionFragment {
    val fragments = this.map { element ->
        when (element) {
            is JsonArray -> element.toCollectionFragment()
            is JsonPrimitive -> DataFragment(element.int)
            else -> error("Unsupported element type ${element::class.simpleName}!")
        }
    }

    return CollectionFragment(fragments)
}

private fun Pair<Packet, Packet>.isOrdered(): Boolean {
    val (lhs, rhs) = this

    return lhs.fragments <= rhs.fragments
}

private operator fun Fragment.compareTo(rhs: Fragment): Int {
    val lhs = this

    if (lhs is DataFragment && rhs is DataFragment)
        return lhs.value.compareTo(rhs.value)

    val left = if (lhs is CollectionFragment) lhs.values else listOf(lhs)
    val right = if (rhs is CollectionFragment) rhs.values else listOf(rhs)

    for (index in 0..(left.lastIndex).coerceAtLeast(right.lastIndex)) {
        when {
            index > left.lastIndex && index <= right.lastIndex ->
                return -1

            index <= left.lastIndex && index > right.lastIndex ->
                return 1

            else -> {
                val result = left[index].compareTo(right[index])

                if (result != 0)
                    return result
            }
        }
    }

    return 0
}

private fun part1(input: List<String>): Int {
    val packetPairs = input.toPacketPairs()

    return packetPairs
        .mapIndexed { index, packetPair -> if (packetPair.isOrdered()) index + 1 else 0 }
        .sum()
}

private fun part2(input: List<String>): Int {
    val packetPairs = input.toPacketPairs().flatMap { (lhs, rhs) -> listOf(lhs, rhs) }
    val dividerPackets = listOf("[[2]]".toPacket(), "[[6]]".toPacket())
    val allPackets = packetPairs + dividerPackets

    val orderedPackets = allPackets.sorted()

    return orderedPackets.mapIndexedNotNull { index, packet ->
        if (packet in dividerPackets) {
            index + 1
        } else null
    }.reduce { a, b -> a * b }
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readTestInput("Day13")
    check(part1(testInput) == 13)
    check(part2(testInput) == 140)

    val input = readInput("Day13")
    println(part1(input))
    println(part2(input))
}
