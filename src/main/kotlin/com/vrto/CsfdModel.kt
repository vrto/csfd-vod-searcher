package com.vrto

interface CsfdService {
    fun getLoginPage(): LoginPage
}

interface LoginPage {
    fun submit(userName: String, password: String): HomePage
    fun skip(): HomePage

    class LoginException : Exception("Invalid login credentials")
}

interface HomePage {
    fun loadRankings(page: Int): List<RankingMovie>
}

abstract class RankingMovie(
    val name: String,
    val link: String
) {
    abstract fun toMovieDetail(): MovieDetail
}

data class MovieDetail(
    val name: String,
    val link: String,
    val availableVODs: List<VOD>,
    val rating: Int,
    val seen: Boolean
)

enum class VOD {
    NETFLIX, APPLE_TV_PLUS, AMAZON_PRIME, HBO_MAX, GOOGLE_PLAY, ITUNES, DISNEY_PLUS;

    companion object
}