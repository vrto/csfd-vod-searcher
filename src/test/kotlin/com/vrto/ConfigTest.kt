package com.vrto

import com.vrto.ConfigStore.SaveResult.*
import com.vrto.FakeCsfdService.Credentials.VALID_LOGIN
import com.vrto.FakeCsfdService.Credentials.VALID_PW
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.assertEquals

class ConfigTest {

    val store: ConfigStore
    val tempFile: Path

    init {
        tempFile = kotlin.io.path.createTempFile()
        store = ConfigStore(FakeCsfdService(), tempFile)
    }

    @Test
    fun `config store should reject invalid credentials`() {
        assertEquals(EMPTY_LOGIN_OR_PASSWORD, store.save("", ""))
        assertEquals(EMPTY_LOGIN_OR_PASSWORD, store.save("", VALID_PW))
        assertEquals(EMPTY_LOGIN_OR_PASSWORD, store.save(VALID_LOGIN, ""))
        assertEquals(INVALID_CREDENTIALS, store.save(VALID_LOGIN, "bogus"))
    }

    @Test
    fun `config store should store valid credentials`() {
        assertEquals(SUCCESSFUL, store.save(VALID_LOGIN, VALID_PW))
        val stored = tempFile.readText()

        // the file is Base64 encoded so that we don't randomly throw login/pw around
        // this isn't cryptography or security, just simple masking
        assertEquals("dmFsaWRUZXN0OnZhbGlkVGVzdFB3", stored)
    }

    @Test
    fun `config store should read the stored credentials`() {
        store.save(VALID_LOGIN, VALID_PW)

        val result = store.read()
        assertEquals(VALID_LOGIN, result.login)
        assertEquals(VALID_PW, result.password)
    }
}