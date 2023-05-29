package com.example.clase8;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UsuarioRepository {

    @FormUrlEncoded
    @POST("guardar")
    Call<UsuarioResponse> guardar(@Field("nombre") String nombre,
                                  @Field("apellido") String apellido,
                                  @Field("dni") String dni,
                                  @Field("email") String email,
                                  @Field("edad") String edad);
}
