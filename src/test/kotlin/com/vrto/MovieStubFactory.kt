package com.vrto

data class MovieStub(
    val name: String,
    val rating: Int,
    val vods: List<VOD>,
    val seen: Boolean
) {
    fun link() = "link/${name.replace(" ", "")}"
}

fun createTestMovies(vararg fakeInputs: MovieStub): List<RankingMovie> {
    return fakeInputs.map { input ->
        object : RankingMovie(name = input.name, link = input.link()) {
            override fun toMovieDetail() = MovieDetail(
                name = input.name,
                link = input.link(),
                rating = input.rating,
                availableVODs = input.vods,
                seen = input.seen
            )
        }
    }
}