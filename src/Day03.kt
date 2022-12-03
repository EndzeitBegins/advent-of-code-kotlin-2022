private data class Rucksack(val items: String) {
    val compartmentA: Compartment = items.take(items.length / 2)
    val compartmentB: Compartment = items.drop(items.length / 2)
}
private typealias Compartment = String
private typealias Item = Char
private typealias ElfGroup = List<Rucksack>

private fun List<String>.asRucksacks(): Sequence<Rucksack> = this
    .asSequence()
    .map { rucksackItems -> Rucksack(rucksackItems) }

private fun Rucksack.findItemsInBothCompartments(): Set<Item> =
    compartmentA.toSet().intersect(compartmentB.toSet())

private fun ElfGroup.findCommonItems(): Set<Item> = this
    .asSequence()
    .map { it.items.toSet() }
    .reduce { acc, curr -> acc.intersect(curr) }

private val Item.priority: Int
    get() = when (code) {
        in 65..90 -> code - 38
        in 97..122 -> code - 96
        else -> error("Character '$this' is not supported!")
    }

private fun part1(input: List<String>): Int = input
    .asRucksacks()
    .flatMap { rucksack -> rucksack.findItemsInBothCompartments() }
    .map { item -> item.priority }
    .sum()

private fun part2(input: List<String>): Int = input
    .asRucksacks()
    .groupElves()
    .flatMap { elfGroup -> elfGroup.findCommonItems() }
    .map { item -> item.priority }
    .sum()

private fun Sequence<Rucksack>.groupElves(): Sequence<ElfGroup> =
    windowed(3, 3, partialWindows = false)

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 157)
    check(part2(testInput) == 70)

    val input = readInput("Day03")
    println(part1(input))
    println(part2(input))
}
