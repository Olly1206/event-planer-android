package com.example.myapplication.network;

import android.content.Context;

import com.example.myapplication.auth.AuthManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    /**
     * BASE_URL — points to Render production backend
     * For local development, change to:
     *   Android Emulator  →  http://10.0.2.2:8080/
     *   Physical device (same Wi-Fi as dev machine)  →  http://<your-machine-LAN-IP>:8080/
     */
    private static final String BASE_URL = "https://event-planer-backend.onrender.com/";  // Render production

    private static Retrofit instance;

    public static Retrofit getInstance(Context context) {
        if (instance == null) {
            // Logging interceptor — prints every HTTP request and response to Logcat
            // Filter by tag "OkHttp" in Logcat to see the raw traffic
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    // Auth interceptor — automatically attaches the JWT to every request
                    // that isn't a login or register call
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String path = original.url().encodedPath();

                        // Auth endpoints are public — no token needed
                        if (path.contains("/api/auth/")) {
                            return chain.proceed(original);
                        }

                        String token = AuthManager.getInstance(context).getToken();
                        if (token != null) {
                            Request authenticated = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                            return chain.proceed(authenticated);
                        }
                        return chain.proceed(original);
                    })
                    .addInterceptor(logging)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    // Call this after logout to force re-creation on next use
    public static void reset() {
        instance = null;
    }
}
