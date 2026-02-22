package com.tensiorr.budgetapp.util

import org.junit.Assert.*
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun `compare returns 1 when new version is greater`() {
        assertEquals(1, VersionComparator.compare("0.3.1", "0.4.0"))
        assertEquals(1, VersionComparator.compare("0.3.1", "v0.4.0"))
        assertEquals(1, VersionComparator.compare("1.0.0", "2.0.0"))
    }

    @Test
    fun `compare returns 0 when versions are equal`() {
        assertEquals(0, VersionComparator.compare("0.3.1", "0.3.1"))
        assertEquals(0, VersionComparator.compare("0.3.1", "v0.3.1"))
    }

    @Test
    fun `compare returns -1 when new version is lower`() {
        assertEquals(-1, VersionComparator.compare("0.4.0", "0.3.1"))
        assertEquals(-1, VersionComparator.compare("2.0.0", "1.0.0"))
    }

    @Test
    fun `isUpdateAvailable returns true when update exists`() {
        assertTrue(VersionComparator.isUpdateAvailable("0.3.1", "0.4.0"))
        assertTrue(VersionComparator.isUpdateAvailable("0.3.1", "v0.4.0"))
    }

    @Test
    fun `isUpdateAvailable returns false when no update`() {
        assertFalse(VersionComparator.isUpdateAvailable("0.4.0", "0.3.1"))
        assertFalse(VersionComparator.isUpdateAvailable("0.4.0", "0.4.0"))
    }
}