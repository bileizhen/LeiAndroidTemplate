package io.github.bileizhen.leitemplate.core.logging

data class LogRecord(val level: String, val text: String)

object LogHistory {
    private val header = Regex("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} ([DIWE])/.*")

    /** Keep exception stack traces attached to the record whose level they inherit. */
    fun parse(raw: String): List<LogRecord> {
        val records = mutableListOf<LogRecord>()
        val lines = mutableListOf<String>()
        var level = "I"
        fun flush() {
            if (lines.isNotEmpty()) records += LogRecord(level, lines.joinToString("\n"))
            lines.clear()
        }
        LogRedactor.redact(raw).lineSequence().forEach { line ->
            val match = header.matchEntire(line)
            if (match != null) {
                flush()
                level = match.groupValues[1]
            }
            if (line.isNotBlank() || lines.isNotEmpty()) lines += line
        }
        flush()
        return records.asReversed()
    }

    fun filter(records: List<LogRecord>, level: String?, query: String): List<LogRecord> =
        records.filter { (level == null || it.level == level) && it.text.contains(query.trim(), ignoreCase = true) }
}
