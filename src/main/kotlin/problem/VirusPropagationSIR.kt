
package problem

import part2.GraphStructure
import java.io.File

enum class State {
    S, I, R
}

data class Computer(
    var state: State = State.S,
    var infectionTime: Int = 0
)

fun loadGraphFromFile(fileName: String): Pair<GraphStructure<Int, Computer>, Pair<Int, Int>> {
    val lines = File(fileName)
        .readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val n = lines[0].toInt()

    val initiallyInfected = lines[lines.size - 2].toInt()
    val tinf = lines[lines.size - 1].toInt()

    val graph = GraphStructure<Int, Computer>()

    for (i in 1..n) {
        graph.addVertex(i, Computer())
    }

    for (i in 1 until lines.size - 2) {
        val parts = lines[i]
            .replace("(", "")
            .replace(")", "")
            .split(Regex("\\s+"))
        val v1 = parts[0].toInt()
        val v2 = parts[1].toInt()

        graph.addEdge(v1, v2)
    }

    return Pair(graph, Pair(initiallyInfected, tinf))
}

fun simulateVirus(fileName: String) {
    val loaded = loadGraphFromFile(fileName)

    val graph = loaded.first
    val initiallyInfected = loaded.second.first
    val tinf = loaded.second.second

    graph.getVertex(initiallyInfected)?.data?.state = State.I

    val output = StringBuilder()
    var instant = 0

    while (hasInfected(graph)) {
        writeInstant(output, graph, instant)

        val toInfect = mutableListOf<Int>()
        val toRecover = mutableListOf<Int>()

        for (vertex in graph) {
            val computer = vertex.data

            if (computer.state == State.I) {
                for (edge in vertex.getAdjacencies()) {
                    val adjacent = graph.getVertex(edge.adjacent)

                    if (adjacent != null && adjacent.data.state == State.S) {
                        toInfect.add(adjacent.id)
                    }
                }

                computer.infectionTime++

                if (computer.infectionTime >= tinf) {
                    toRecover.add(vertex.id)
                }
            }
        }

        for (id in toInfect) {
            val vertex = graph.getVertex(id)

            if (vertex != null && vertex.data.state == State.S) {
                vertex.data.state = State.I
                vertex.data.infectionTime = 0
            }
        }

        for (id in toRecover) {
            val vertex = graph.getVertex(id)

            if (vertex != null) {
                vertex.data.state = State.R
            }
        }

        instant++
    }

    writeInstant(output, graph, instant)

    File("simulaçãoVirus.txt").writeText(output.toString())
}

fun hasInfected(graph: GraphStructure<Int, Computer>): Boolean {
    for (vertex in graph) {
        if (vertex.data.state == State.I) return true
    }

    return false
}

fun writeInstant(
    output: StringBuilder,
    graph: GraphStructure<Int, Computer>,
    instant: Int
) {
    val susceptible = mutableListOf<Int>()
    val infected = mutableListOf<Int>()
    val recovered = mutableListOf<Int>()

    for (vertex in graph) {
        when (vertex.data.state) {
            State.S -> susceptible.add(vertex.id)
            State.I -> infected.add(vertex.id)
            State.R -> recovered.add(vertex.id)
        }
    }

    susceptible.sort()
    infected.sort()
    recovered.sort()

    output.appendLine("Instante $instant")
    output.appendLine("S = ${formatSet(susceptible)}")
    output.appendLine("I = ${formatSet(infected)}")
    output.appendLine("R = ${formatSet(recovered)}")
    output.appendLine()
}

fun formatSet(values: List<Int>): String {
    return values.joinToString(
        prefix = "{",
        postfix = "}",
        separator = ","
    )
}

fun main() {
    print("Nome do ficheiro .gr: ")
    val fileName = readln()
    simulateVirus(fileName)
}



