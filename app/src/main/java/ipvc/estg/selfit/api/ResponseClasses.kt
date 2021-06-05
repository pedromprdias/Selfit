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

data class AllAlimentosOutput (
        val error: String?,
        val listaAlimentos: List<Alimento>?
)

data class AlimentoOutput (
        val error: String?,
        val alimento: Alimento?
)

data class Alimento (
        val id: Int,
        val nome: String,
        val descricao: String?,
        val tipo: String?,
        val calorias: Float?,
        val lipidos: Float?,
        val proteinas: Float?,
        val hidratosCarbono: Float?,
        val imagem: Imagem
)

data class Imagem (
        val type: String,
        val data: ByteArray
)