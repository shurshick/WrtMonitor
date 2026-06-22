package ru.wrtmonitor.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentCapabilitiesTest {
    @Test
    fun summaryShowsEnabledAndDisabledCounts() {
        assertEquals(
            "2 enabled / 1 disabled",
            capabilitiesSummary(
                mapOf(
                    "agent.update" to true,
                    "wifi.set_ssid" to true,
                    "system.reboot" to false,
                ),
            ),
        )
    }

    @Test
    fun groupedCapabilitiesUseExpectedSections() {
        val grouped = groupedCapabilities(
            mapOf(
                "wifi.set_ssid" to true,
                "agent.update" to true,
                "diagnostics.check_dns" to true,
            ),
        )

        assertEquals("Agent", grouped[0].first)
        assertTrue(grouped[0].second.contains("agent.update"))
        assertEquals("Wi-Fi", grouped[1].first)
        assertTrue(grouped[1].second.contains("wifi.set_ssid"))
        assertEquals("Diagnostics", grouped[2].first)
        assertTrue(grouped[2].second.contains("diagnostics.check_dns"))
    }
}
