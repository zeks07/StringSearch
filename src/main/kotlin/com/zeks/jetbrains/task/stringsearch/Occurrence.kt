package com.zeks.jetbrains.task.stringsearch

import java.nio.file.Path

interface Occurrence {
    val file: Path
    val line: Int
    val offset: Int
}

data class SimpleOccurrence(
    override val file: Path,
    override val line: Int,
    override val offset: Int,
) : Occurrence