package com.example.chatapp.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "COntent-Type:Application/json" ,
            "Authorization:key=AAAAiM_-H44:APA91bGpk_7dxyir_65GO75cfLwm_MQdAf_jSZjWoB1oU18AYZeiQfrUCaMNr8yXkufMJzDobzUmrOo50PfRhRkYkBskektaw7UjjbwklOt4Ij9_pqExRNJkwZjYNs08pnbSwD8TvJmE"
    })
    @POST("fcm/send")
    Call<Response>sendNotification(@Body Sender body);
}
