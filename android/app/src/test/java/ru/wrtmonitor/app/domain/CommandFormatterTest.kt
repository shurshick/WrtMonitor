package ru.wrtmonitor.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CommandFormatterTest {
    @Test fun expiredIsReadable() = assertEquals("Истекла", CommandFormatter.statusLabel("expired"))
}
