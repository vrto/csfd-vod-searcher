package com.vrto

import com.vrto.LoginPage.LoginException

class FakeCsfdService : CsfdService {

    companion object Credentials {
        val VALID_LOGIN = "validTest"
        val VALID_PW = "validTestPw"
    }

    override fun getLoginPage() = object : LoginPage {
        override fun submit(userName: String, password: String): HomePage {
            if (userName == VALID_LOGIN && password == VALID_PW) {
                return fakeHomePage()
            }
            throw LoginException()
        }

        override fun skip() = fakeHomePage()
    }

    private fun fakeHomePage() = object : HomePage {
        override fun loadRankings(page: Int): List<RankingMovie> {
            return emptyList()
        }
    }
}