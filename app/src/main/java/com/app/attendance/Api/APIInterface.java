package com.app.attendance.Api;
import com.app.attendance.Model.StoreListResponse;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("stores")
    Observable<StoreListResponse> getStoreList(@Query("page") Integer page);

    @Multipart
    @POST("attendance")
    Observable<Response<ResponseBody>> submitData(@Part("name") String name,
                                                  @Part("uid") String uid,
                                                  @Part("latitude") Double latitude,
                                                  @Part("longitude") Double longitude,
                                                  @Part("request_id") String request_id);

}
