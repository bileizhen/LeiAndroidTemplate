package io.github.bileizhen.leitemplate.core.logging

import org.junit.Assert.*
import org.junit.Test

class LogHistoryTest {
    @Test fun secretsAreRedactedInHeadersJsonAndUrls() {
        val secret = "test-secret-XYZ"
        listOf("Authorization: Bearer $secret", "Cookie: session=$secret; other=$secret",
            "https://example.com/?token=$secret&password=$secret",
            "{\"access_token\":\"$secret\",\"password\":\"$secret\"}",
            "request failed with Bearer $secret").forEach {
            val safe = LogRedactor.redact(it)
            assertFalse(safe, safe.contains(secret))
            assertTrue(safe, safe.contains("[REDACTED]"))
        }
    }

    @Test fun stackTracesInheritTheLevelAndFiltersKeepWholeRecords() {
        val raw = "2026-10-03 12:00:00.000 I/Start: ready\n" +
            "2026-10-03 12:00:01.000 E/Network: failed token=hidden\n" +
            "java.io.IOException: offline\n  at sample.request(Network.kt:10)\n"
        val records = LogHistory.parse(raw)
        assertEquals(2, records.size)
        assertEquals("E", records.first().level)
        assertTrue(records.first().text.contains("IOException"))
        assertFalse(records.first().text.contains("hidden"))
        assertEquals(1, LogHistory.filter(records, "E", "OFFLINE").size)
        assertTrue(LogHistory.filter(records, "I", "offline").isEmpty())
    }
}
