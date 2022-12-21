package com.vrto

import com.vrto.VOD.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SearchingTest {

    val input = createTestMovies(
        MovieStub(name = "Pulp Fiction", rating = 92, vods = listOf(NETFLIX, AMAZON_PRIME), seen = true),
        MovieStub(name = "Pelíšky", rating = 88, vods = listOf(HBO_MAX), seen = false),
        MovieStub(name = "Clockwork Orange", rating = 90, vods = listOf(NETFLIX, HBO_MAX), seen = false),
    )

    @Test
    fun `movie search should filter by VOD type`() {
        val results = searchMovies(input, SearchFilter(vod = NETFLIX))
        val expected = listOf(
            SearchResult(name = "Pulp Fiction", rating = 92, link = "link/PulpFiction"),
            SearchResult(name = "Clockwork Orange", rating = 90, link = "link/ClockworkOrange"),
        )

        assertEquals(expected, results)
    }

    @Test
    fun `movie search should filter by VOD type and 'seen' flag`() {
        val results = searchMovies(input, SearchFilter(vod = NETFLIX, unseenOnly = true))
        val expected = listOf(SearchResult(name = "Clockwork Orange", rating = 90, link = "link/ClockworkOrange"))

        assertEquals(expected, results)
    }
}