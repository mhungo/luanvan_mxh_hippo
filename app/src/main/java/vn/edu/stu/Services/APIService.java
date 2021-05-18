package vn.edu.stu.Services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import vn.edu.stu.Model.MyResponse;
import vn.edu.stu.Model.Sender;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAvqaT5P8:APA91bETf-ELnQatXkJSEWDMdVS6djNA_yOtz2H_ejv1k6Qyy8v1KBvtv6XB7zmfzwbopdH_iyycvW0CHiC6A3IZD2eLxumN0a4fqHpJVpmvPF7l2y5Ia9QK9erkeoh_Z96Cm2ZjW5p2"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
