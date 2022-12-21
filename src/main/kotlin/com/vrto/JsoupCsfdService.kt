package com.vrto

import com.vrto.VOD.*
import org.jsoup.Connection
import org.jsoup.Connection.Method.POST
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class JsoupCsfdService : CsfdService {

    override fun getLoginPage(): LoginPage {
        val connection = Jsoup.connect("https://cas.csfd.cz/login?s=Vzu0qUOmByjiKP93q3phL3AzMP5wrvV")
        return JsoupLoginPage(connection)
    }
}

class JsoupLoginPage(private val connection: Connection) : LoginPage {
    override fun submit(userName: String, password: String): HomePage {
        val response = connection
            .data("username", userName)
            .data("password", password)
            .method(POST)
            .execute()
            .takeUnless { it.body().contains("Přezdívka nebo heslo jsou nesprávné.") }
            ?: throw LoginPage.LoginException()

        return response.let {
            println("Logged into CSFD.cz as $userName")
            JsoupHomePage(it.cookies())
        }
    }

    override fun skip(): HomePage {
        println("Using CSFD.cz without login")
        return JsoupHomePage(emptyMap())
    }
}

class JsoupHomePage(private val cookies: Map<String, String>) : HomePage {
    override fun loadRankings(page: Int): List<RankingMovie> {
        val from = when (page) {
            0,1 -> 1
            else -> 100 * (page - 1)
        }

        val wrapper = Jsoup.connect("https://www.csfd.cz/zebricky/filmy/nejlepsi?from=$from")
            .get()
            .select("#tabs > div > div > div > div > div.column.column-minus-300 > section > div")
            .first() ?: throw SelectionException("Failed to select the ranking wrapper component")

        val articles = wrapper.select("article")
        val links = articles.map { it.select("a") }
        return links.map {
            JsoupRankingMovie(
                cookies,
                name = it.attr("title"),
                link = it.attr("href")
            )
        }
    }
}

class JsoupRankingMovie(private val cookies: Map<String, String>, name: String, link: String) : RankingMovie(name, link) {
    override fun toMovieDetail(): MovieDetail {
        val moviePage = Jsoup.connect("https://www.csfd.cz$link").cookies(cookies).get()
        return MovieDetail(
            name = name,
            link = link,
            availableVODs = moviePage.selectVods(),
            rating = moviePage.selectRating(),
            seen = moviePage.selectSeen(),
        )
    }
}

private fun Document.selectVods(): List<VOD> {
    val vods = this.select("a")
        .toList()
        .filter { it.attr("data-ga-event").contains("film|vod") }
        .map(Element::text)

    return vods.mapNotNull { vod ->
        when (vod) {
            "Apple TV+" -> APPLE_TV_PLUS
            "Prime Video" -> AMAZON_PRIME
            "HBO Max" -> HBO_MAX
            "Netflix" -> NETFLIX
            "Google Play" -> GOOGLE_PLAY
            "iTunes" -> ITUNES
            "Disney+" -> DISNEY_PLUS
            else -> null
        }
    }.distinct()
}

private fun Document.selectRating(): Int {
    val rating = this.select("#page-wrapper > div > div.main-movie > aside > div.box-rating-container > div.box-rating.box-rating-withtabs > div.film-rating-average")
        .first() ?: throw SelectionException("Failed to select the movie rating")

    // rating sample: 85%
    return rating.text()
        .dropLast(1) // percent sign
        .toInt()
}

private fun Document.selectSeen(): Boolean {
    val myRating = this.select("#page-wrapper > div > div.main-movie > aside > div > div.box-rating.box-rating-withtabs > div.my-rating > span")
    return when {
        myRating.isEmpty() -> false
        myRating.first()!!.attr("title").contains("Vloženo") -> true
        else -> false
    }
}

class SelectionException(msg: String) : Exception(msg)