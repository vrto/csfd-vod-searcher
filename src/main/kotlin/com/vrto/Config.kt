package com.vrto

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.vrto.ConfigStore.SaveResult.*
import com.vrto.LoginPage.LoginException
import java.nio.file.Path
import java.util.Base64
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ConfigCommand(private val configStore: ConfigStore) : CliktCommand(help = "Configure CSFD credentials") {

    private val login: String by argument()
    private val password: String by argument()

    override fun run() {
        echo(configStore.save(login, password))
    }
}

class ConfigStore(private val csfdService: CsfdService, private val credentialsPath: Path) {

    fun save(login: String, password: String): SaveResult {
        if (login.isEmpty() || password.isEmpty()) {
            return EMPTY_LOGIN_OR_PASSWORD
        }

        val loginPage = csfdService.getLoginPage()
        try {
            loginPage.submit(login, password)
        } catch (e: LoginException) {
            return INVALID_CREDENTIALS
        }

        val base64 = Base64.getEncoder().encodeToString("$login:$password".toByteArray())
        credentialsPath.writeText(base64)
        return SUCCESSFUL
    }

    enum class SaveResult {
        EMPTY_LOGIN_OR_PASSWORD, INVALID_CREDENTIALS, SUCCESSFUL
    }

    fun read(): ReadResult {
        // the program always ensures that the config file exists upon startup
        val base64 = credentialsPath.readText()
        val decoded = String(Base64.getDecoder().decode(base64))
        val split = decoded.split(":")
        return ReadResult(login = split[0], password = split[1])
    }

    data class ReadResult(
        val login: String,
        val password: String,
    )
}