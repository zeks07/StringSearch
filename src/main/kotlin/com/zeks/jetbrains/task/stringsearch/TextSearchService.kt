package com.zeks.jetbrains.task.stringsearch

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class TextSearchService() {
    private var job: Job? = null
    var toolWindowContent: TextSearchToolWindowContent? = null

    fun run(
        stringToSearch: String,
        directory: Path,
        onResult: (Occurrence) -> Unit,
    ) {
        if (job?.isActive == true) return

        job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            searchForTextOccurrences(stringToSearch, directory).collect { result ->
                onResult(result)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun isRunning() = job?.isActive == true
}

fun searchForTextOccurrences(
    stringToSearch: String,
    directory: Path
): Flow<Occurrence> = flow {
    val stack = ArrayDeque<Path>()
    stack.add(directory)

    while (stack.isNotEmpty()) {
        val current = stack.removeLast()

        if (Files.isSymbolicLink(current) || current.toString().endsWith(".lnk")) continue

        if (Files.isDirectory(current)) {
            Files.list(current).use { stream ->
                stream.forEach { stack.add(it) }
            }
        } else {
            val text: String = try {
                Files.readString(current)
            } catch (_: Exception) {
                continue
            }.replace("\r\n", "\n")

            var searchIndex = 0

            while (true) {
                val index = text.indexOf(stringToSearch, searchIndex)
                if (index == -1) break

                val line = text.take(index).count { it == '\n' } + 1
                val column = index - (text.lastIndexOf('\n', index - 1).takeIf { it != -1 } ?: -1)

                emit(SimpleOccurrence(current, line, column))
                searchIndex = index + stringToSearch.length
            }
        }
    }
}
