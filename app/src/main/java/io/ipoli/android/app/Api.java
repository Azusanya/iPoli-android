package io.ipoli.android.app;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.ApiConstants;
import io.ipoli.android.player.AuthProvider;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/17.
 */

public class Api {

    private final ObjectMapper objectMapper;
    private OkHttpClient httpClient;


    public Api(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        httpClient = new OkHttpClient().newBuilder().addInterceptor(chain -> {
            Log.i("REQUEST INFO", chain.request().url().toString());
            Log.i("REQUEST INFO", chain.request().headers().toString());

            Response response = chain.proceed(chain.request());
            Log.i("RESPONSE INFO", response.toString());
            Log.i("RESPONSE INFO", response.body().toString());
            Log.i("RESPONSE INFO", response.message());

            return chain.proceed(chain.request());
        }).build();
    }

    public void createSession(AuthProvider authProvider, String accessToken, String email, SessionResponseListener responseListener) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        Map<String, String> params = new HashMap<>();
        params.put("auth_provider", authProvider.getProvider());
        params.put("auth_id", authProvider.getId());
        params.put("access_token", accessToken);
        params.put("email", email);

        JSONObject jsonObject = new JSONObject(params);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(ApiConstants.URL).post(body);

        httpClient.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                responseListener.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
                    };
                    Map<String, Object> session = objectMapper.convertValue(response.body().charStream(), mapTypeReference);
                    String username = (String) session.get("username");
                    String email = (String) session.get("email");
                    List<Cookie> cookies = Cookie.parseAll(HttpUrl.get(getUrl(ApiConstants.URL)), response.headers());
                    boolean newUserCreated = (boolean) session.get("is_new");
                    responseListener.onSuccess(username, email, cookies, newUserCreated);
                }
            }
        });
    }

    private URL getUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public interface SessionResponseListener {
        void onSuccess(String username, String email, List<Cookie> cookies, boolean newUserCreated);

        void onError(Exception e);
    }
}
