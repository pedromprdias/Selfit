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
}