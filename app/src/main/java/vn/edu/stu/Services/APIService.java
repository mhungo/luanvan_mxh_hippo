package vn.edu.stu.Services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import vn.edu.stu.Model.ModelCity;
import vn.edu.stu.Model.MyResponse;
import vn.edu.stu.Model.Sender;
import vn.edu.stu.Model.SenderTypeCall;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAY-i3_Yw:APA91bErcjLbKrVDrbNKVDQ5ztwgVVx5174JUkVgtU-1vNWPp7XZ6khuKNMjMNvYXDnjuMKAdUrvZLMtjDzbwf5nWqySdxyvLQccQHUQgL7bZrde53kRVXBpldy_PUme57AiX-uR5Sw_"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

    @POST("fcm/send")
    Call<String> sendRemoteMessage(@Body SenderTypeCall senderTypeCall);

    @GET("city")
    Call<ModelCity> getCity();

}
