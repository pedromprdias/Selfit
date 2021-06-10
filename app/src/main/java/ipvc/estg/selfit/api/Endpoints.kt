package ipvc.estg.selfit.api

import retrofit2.Call
import retrofit2.http.*

interface Endpoints {

    //login endpoint
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginOutput>

    //logout endpoint
    @DELETE("logout")
    fun logout(
        @Header("Authorization") authorization: String
    ): Call<LogoutOutput>

    //get all food endpoint
    @GET("alimentos")
    fun getAllAlimentos(
            @Header("Authorization") authorization: String
    ): Call<AllAlimentosOutput>

    //get all the info of a food item
    @GET("alimentos/{id}")
    fun getAlimento(
            @Path("id") id: Int,
            @Header("Authorization") authorization: String
    ): Call<AlimentoOutput>

    //get all exercises endpoint
    @GET("exercicios")
    fun getAllExercicios(
            @Header("Authorization") authorization: String
    ): Call<AllExerciciosOutput>

    //get all the info of an exercise
    @GET("exercicios/{id}")
    fun getExercicio(
            @Path("id") id: Int,
            @Header("Authorization") authorization: String
    ): Call<ExercicioOutput>

    //get all the info of a machine
    @GET("maquinas/{id}")
    fun getMaquina(
            @Path("id") id: Int,
            @Header("Authorization") authorization: String
    ): Call<MaquinaOutput>

    //get all the info of a day
    @GET("registos")
    fun getRegisto(
            @Header("Authorization") authorization: String,
            @Query("data") data: String
    ): Call<RegistoOutput>

    //post the information of a meal
    @POST("refeicoes")
    fun postRefeicao(
            @Header("Authorization") authorization: String,
            @Body body: RefeicaoInput
    ): Call<PostOutput>

    //change the information of a meal
    @PUT("refeicoes/{id}")
    fun putRefeicao(
            @Header("Authorization") authorization: String,
            @Path("id") id: Int,
            @Body body: RefeicaoInput
    ): Call<PostOutput>

    //post the information of a day's workout
    @POST("treinosDiarios")
    fun postTreinoDiario(
            @Header("Authorization") authorization: String,
            @Body body: TreinoDiarioInput
    ): Call<PostOutput>

    //change the information of a day's workout
    @PUT("treinosDiarios/{id}")
    fun putTreinoDiario(
            @Header("Authorization") authorization: String,
            @Path("id") id: Int,
            @Body body: TreinoDiarioInput
    ): Call<PostOutput>

    //get the last registed body measure
    @GET("ultimaMedida")
    fun getUltimaMedida(
            @Header("Authorization") authorization: String
    ): Call<MedidaOutput>

    //post body measures
    @POST("medidas")
    fun postMedida(
            @Header("Authorization") authorization: String,
            @Body body: Medida
    ): Call<PostOutput>
}