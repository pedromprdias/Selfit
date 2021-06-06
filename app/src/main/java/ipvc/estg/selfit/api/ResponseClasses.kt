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

data class AllExerciciosOutput (
        val error: String?,
        val listaExercicios: List<Exercicio>?
)

data class ExercicioOutput (
        val error: String?,
        val exercicio: Exercicio?
)

data class Exercicio (
        val id: Int,
        val nome: String,
        val musculos: List<Musculo>,
        val descricao: String?,
        val maquina: Maquina?,
        val maxPeso: Float?,
        val dataMaxPeso: String?,
        val maxOverall: Peso?,
        val dataMaxOverall: String?,
        val imagem: Imagem
)

data class Peso (
        val peso: Float,
        val series: Int,
        val repeticoes: Int
)

data class Maquina (
        val id: Int,
        val nome: String
)

data class Musculo (
        val id: Int,
        val nome: String
)

data class Imagem (
        val type: String,
        val data: ByteArray
)