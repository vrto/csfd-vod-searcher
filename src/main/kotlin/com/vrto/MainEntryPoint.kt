package com.vrto

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import java.io.File

fun main(args: Array<String>) {
    val configStore = createConfigStore()
    CsfdCommand.subcommands(
        ConfigCommand(configStore),
        SearchCommand(JsoupCsfdService(), configStore)
    ).main(args)
}

object CsfdCommand : CliktCommand(help = "CSFD VOD searcher") {
    override fun run() {
        // wrapper command
    }
}

private fun createConfigStore(): ConfigStore {
    val cfg = File(".csfd.cfg")
    if (!cfg.exists()) {
        cfg.createNewFile()
    }
    return ConfigStore(JsoupCsfdService(), cfg.toPath())
}
