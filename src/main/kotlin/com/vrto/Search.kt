package com.vrto

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.vrto.VOD.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream

class SearchCommand(private val csfdService: CsfdService, private val configStore: ConfigStore) :
    CliktCommand(help = "Search movies by VOD availability") {

    private val vod: String by argument().choice("Netflix", "HBOMax", "AppleTV+", "AmazonPrime", "GooglePlay", "iTunes", "Disney+")
    private val page: Int by option().int().default(1)
    private val unseenOnly: Boolean by option(help = "SLOW! Re-uses user cookies across the calls, needs to process movies sequentially to avoid rate limiting.")
        .flag(default = false)
    private val sorted: Boolean by option(help = "SLOW! Sorts the results, BUT blocks until the ENTIRE search is done!")
        .flag(default = false)

    override fun run() {
        val loginPage = csfdService.getLoginPage()
        val homePage = if (unseenOnly) {
            val (login, pw) = configStore.read()
            loginPage.submit(login, pw)
        } else {
            loginPage.skip()
        }

        val rankings = homePage.loadRankings(page)

        val filter = SearchFilter(vod = VOD.fromString(vod), unseenOnly = unseenOnly, sorted = sorted)
        if (sorted) {
            searchMovies(rankings, filter).sortedByDescending(SearchResult::rating).forEach(::println)
        } else {
            searchMoviesStream(rankings, filter).forEach(::println)
        }
    }
}

private fun Companion.fromString(vod: String): VOD = when (vod) {
    "Netflix" -> NETFLIX
    "HBOMax" -> HBO_MAX
    "AppleTV+" -> APPLE_TV_PLUS
    "AmazonPrime" -> AMAZON_PRIME
    "GooglePlay" -> GOOGLE_PLAY
    "iTunes" -> ITUNES
    "Disney+" -> DISNEY_PLUS
    else -> throw IllegalArgumentException("Illegal VOD value")
}

fun searchMoviesStream(rankings: List<RankingMovie>, filter: SearchFilter): Stream<SearchResult> {
    // unseenOnly is computed based on the logged-in user rating,
    // when unseenOnly=true then we can't process in parallel in order to avoid rate limits
    val stream = when (filter.unseenOnly) {
        true -> rankings.stream()
        false -> rankings.parallelStream()
    }

    val printProgress = filter.unseenOnly || filter.sorted

    val counter = AtomicInteger(0)
    return stream
        .map(RankingMovie::toMovieDetail)
        .map {
            if (printProgress) {
                counter.addAndGet(1)
                if (counter.get() % 10 == 0) {
                    println("$counter % done!")
                }
            }
            it
        }
        .filter { it.matches(filter) }
        .map(MovieDetail::toSearchResult)
}

fun searchMovies(rankings: List<RankingMovie>, filter: SearchFilter): List<SearchResult> =
    searchMoviesStream(rankings, filter).toList()

private fun MovieDetail.toSearchResult() = SearchResult(name = name, rating = rating, link = link)

private fun MovieDetail.matches(filter: SearchFilter): Boolean = when {
    filter.unseenOnly -> !this.seen && this.availableVODs.contains(filter.vod)
    else -> this.availableVODs.contains(filter.vod)
}

data class SearchFilter(
    val vod: VOD,
    val unseenOnly: Boolean = false,
    val sorted: Boolean = false
)

data class SearchResult(
    val name: String,
    val rating: Int,
    val link: String
) {
    override fun toString() = "$name ($rating%)\n\thttp://csfd.cz$link"
}