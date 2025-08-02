package com.inhatc.localit.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TourApiService {

    @GET("areaBasedList1")
    Call<TourResponse> getTourList(
            @Query("serviceKey") String serviceKey,
            @Query("MobileOS") String mobileOS,
            @Query("MobileApp") String mobileApp,
            @Query("arrange") String arrange,
            @Query("contentTypeId") int contentTypeId,
            @Query("areaCode") int areaCode,
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("_type") String type
    );
}
