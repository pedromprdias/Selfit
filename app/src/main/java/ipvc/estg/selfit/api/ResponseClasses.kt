package ipvc.estg.selfit.api

data class LoginOutput (
    val id: Int?,
    val username: String?,
    val error: String?,
    val accessToken: String?
)

data class LogoutOutput (
    val error: String?,
    val msg: String?
)