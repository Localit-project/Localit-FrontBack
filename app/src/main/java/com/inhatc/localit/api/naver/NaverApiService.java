// NaverApiService.java

package com.inhatc.localit.api.naver;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface NaverApiService {

    // 1단계에서 찾은 정보를 코드로 옮긴 결과
    @GET("v1/search/news.json")
    Call<NaverNewsResponse> getNews(
            @Header("X-Naver-Client-Id") String clientId,
            @Header("X-Naver-Client-Secret") String clientSecret,
            @Query("query") String query,
            @Query("display") int display,
            @Query("sort") String sort
    );
}