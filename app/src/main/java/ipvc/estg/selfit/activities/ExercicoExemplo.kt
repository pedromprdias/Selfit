package ipvc.estg.selfit.activities

data class ExercicoExemplo (
        val imageResource: Int,
        val text: String
)

data class AllExericicosOutput (
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
        val imagem: Image
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

data class Image (
        val type: String,
        val data: ByteArray
)