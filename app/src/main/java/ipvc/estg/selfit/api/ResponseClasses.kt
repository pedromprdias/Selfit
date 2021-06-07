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

data class MaquinaOutput (
        val error: String?,
        val maquina: Maquina?
)

data class AllExerciciosOutput (
        val error: String?,
        val listaExercicios: List<Exercicio>?
)

data class ExercicioOutput (
        val error: String?,
        val exercicio: Exercicio?
)

/*********************************************************/

data class RefeicaoInput(
        val data: String,
        val tipo: String,
        val alimentos: MutableList<AlimentoInput>
)

data class AlimentoInput(
        val id: Int,
        val quantidade: Int
)

data class TreinoDiarioInput(
        val data: String,
        val exercicios: MutableList<ExercicioInput>
)

data class ExercicioInput(
        val id: Int,
        val series: Int,
        val repeticoes: Int,
        val peso: Float
)

/*********************************************************/

data class Alimento (
        val id: Int,
        val nome: String? = null,
        val descricao: String? = null,
        val tipo: String? = null,
        val calorias: Float? = null,
        val lipidos: Float? = null,
        val proteinas: Float? = null,
        val hidratosCarbono: Float? = null,
        val imagem: Imagem? = null,
        val quantidade: Int? = null
)

data class Exercicio (
        val id: Int,
        val nome: String? = null,
        val musculos: List<Musculo>?  = null,
        val descricao: String? = null,
        val maquina: Maquina? = null,
        val maxPeso: Float? = null,
        val dataMaxPeso: String? = null,
        val maxOverall: Peso? = null,
        val dataMaxOverall: String? = null,
        val imagem: Imagem? = null,
        var valores: Peso? = null
)

data class Peso (
        val peso: Float,
        val series: Int,
        val repeticoes: Int
)

data class Maquina (
        val id: Int,
        val nome: String,
        val imagem: Imagem,
        val exercicios: List<Exercicio>?,
        val descricao: String?
)

data class Musculo (
        val imagem: Imagem,
        val nome: String
)

data class Imagem (
        val type: String,
        val data: ByteArray
)