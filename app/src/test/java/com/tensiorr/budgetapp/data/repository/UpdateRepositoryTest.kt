package com.tensiorr.budgetapp.data.repository

import org.junit.Assert.*
import org.junit.Test

class UpdateRepositoryTest {

    @Test
    fun `parseVersionCode converts version string correctly`() {
        val repo = UpdateRepository()

        val method = UpdateRepository::class.java.getDeclaredMethod(
            "parseVersionCode",
            String::class.java
        )
        method.isAccessible = true

        assertEquals(400L, method.invoke(repo, "0.4.0"))
        assertEquals(305L, method.invoke(repo, "0.3.5"))
        assertEquals(301L, method.invoke(repo, "0.3.1"))
        assertEquals(10000L, method.invoke(repo, "1.0.0"))
        assertEquals(10203L, method.invoke(repo, "1.2.3"))
    }
}