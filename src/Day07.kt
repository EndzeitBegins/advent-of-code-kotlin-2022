import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

private fun List<String>.toFileIndex(): Map<Path, Int> {
    var path = Path("/")
    val fileIndex = mutableMapOf<Path, Int>()

    for (line in this) {
        if (line.startsWith("$ cd")) {
            val target = line.drop(5)
            path = path.resolve(target).normalize()
        } else {
            val listedFileRegex = """^(\d+) (.+)$""".toRegex()
            val regexMatch = listedFileRegex.matchEntire(line)
            if (regexMatch != null) {
                val (fileSize, fileName) = regexMatch.destructured
                val filePath = path / fileName

                fileIndex[filePath] = fileSize.toInt()
            }
        }
    }

    return fileIndex
}

private fun Map<Path, Int>.crawlForDirectories(): Set<Path> {
    val directories: Set<Path> = flatMap { (file, _) ->
        val directories = mutableListOf<Path>()
        var path: Path? = file.parent
        while (path != null) {
            directories.add(path)
            path = path.parent
        }

        directories
    }.toSet()
    return directories
}

private fun Set<Path>.calculateSizes(
    fileIndex: Map<Path, Int>
): Map<Path, Int> {
    val directorySizes: Map<Path, Int> = associateWith { directoryPath ->
        fileIndex
            .filterKeys { filePath -> filePath.startsWith(directoryPath) }
            .values
            .sum()
    }
    return directorySizes
}

private fun part1(input: List<String>): Int {
    val fileIndex = input.toFileIndex()
    val directories: Set<Path> = fileIndex.crawlForDirectories()
    val directorySizes: Map<Path, Int> = directories.calculateSizes(fileIndex)

    return directorySizes
        .filterValues { directorySize -> directorySize <= 100000 }
        .values
        .sum()
}

private fun part2(input: List<String>): Int {
    val fileIndex = input.toFileIndex()
    val directories: Set<Path> = fileIndex.crawlForDirectories()
    val directorySizes: Map<Path, Int> = directories.calculateSizes(fileIndex)

    val spaceAvailable = 70000000
    val spaceUsed = directorySizes.values.max()
    val spaceFree = spaceAvailable - spaceUsed

    val spaceRequired = 30000000
    val spaceToClear = spaceRequired - spaceFree

    return directorySizes
        .filterValues { directorySize -> directorySize >= spaceToClear }
        .minBy { (_, directorySize) -> directorySize }
        .value
}

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 95437)
    check(part2(testInput) == 24933642)

    val input = readInput("Day07")
    println(part1(input))
    println(part2(input))
}
