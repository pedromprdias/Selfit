package ipvc.estg.selfit.api

data class LoginOutput (
    val id: Int?,
    val username: String?,
    val error: String?,
    val accessToken: String?
)