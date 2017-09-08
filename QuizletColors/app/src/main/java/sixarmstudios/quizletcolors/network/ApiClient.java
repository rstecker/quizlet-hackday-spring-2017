package sixarmstudios.quizletcolors.network;

import android.arch.persistence.room.util.StringUtil;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import quizlet.QOAuthResponse;
import quizlet.QSet;
import quizlet.QUser;

/**
 * Created by rebeccastecker on 6/12/17.
 */


public class ApiClient {
    public static final String TAG = ApiClient.class.getSimpleName();
    private static final String SET_URL = "https://api.quizlet.com/2.0/sets/%d?client_id=%s";
    private static final String USER_DEETS_URL = "https://api.quizlet.com/2.0/users/%s";
    private OkHttpClient mClient;
    private String mToken;
    private String mUsername;
    private final BehaviorProcessor<QSet> mQSetProcessor = BehaviorProcessor.create();
    private final BehaviorProcessor<QUser> mQUserProcessor = BehaviorProcessor.create();

    ApiClient(String encodedString) {
        mClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .addHeader("Authorization", getApproriateAuth(encodedString))
                        .build()
                ))
                .build();
    }

    private String getApproriateAuth(String encodedString) {
        return StringUtils.isEmpty(mToken) ? "Basic " + encodedString : "Bearer " + mToken;
    }

    String fetchSet(long setId, @NonNull String clientId) {
        Request request = new Request.Builder()
                .url(String.format(Locale.ENGLISH, SET_URL, setId, clientId))
                .build();
        try {
            try (Response response = mClient.newCall(request).execute()) {
                Log.v(TAG, "Response from server : " + response);
                if (response != null && response.body() != null) {
                    String jsonResponse = response.body().string();
                    QSet qset = convertResponseToQSet(jsonResponse);
                    mQSetProcessor.onNext(qset);
                } else {
                    Log.e(TAG, "Error encountered trying to load set " + setId + " : " + response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private QSet convertResponseToQSet(String response) {
        try {
            return new ObjectMapper().readValue(response, QSet.class);
        } catch (IOException e) {
            Log.e(TAG, "Failed to convert response to QSet " + response);
            e.printStackTrace();
        }
        return null;
    }

    Flowable<QSet> getQSetFlowable() {
        return mQSetProcessor;
    }
    Flowable<QUser> getQUserFlowable() {
        return mQUserProcessor;
    }


    void handleOAuthCode(String authCode, String redirectUrl) {
        mToken = null;
        Request request = new Request.Builder()
                .url("https://api.quizlet.com/oauth/token")
                .method("POST", RequestBody.create(null, new byte[0]))
                .post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("code", authCode)
                        .addFormDataPart("redirect_uri", redirectUrl)
                        .addFormDataPart("grant_type", "authorization_code")
                        .build())
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            Log.v(TAG, "Response from server : " + response);
            if (response != null && response.body() != null) {
                String jsonResponse = response.body().string();
                QOAuthResponse oAuthResponse = convertResponseToOAuth(jsonResponse);
                Log.v(TAG, "OAuth response : " + jsonResponse + " :: " + oAuthResponse);
                if (oAuthResponse != null) {
                    mToken = oAuthResponse.accessToken();
                    mUsername = oAuthResponse.username();
                    Log.i(TAG, "Access Token successfully set on client for " + mUsername);
                    updateUserInfo();
                }
            } else {
                Log.e(TAG, "Error encountered trying to parse access code " + authCode + " @ " + redirectUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private QOAuthResponse convertResponseToOAuth(String response) {
        try {
            return new ObjectMapper().readValue(response, QOAuthResponse.class);
        } catch (IOException e) {
            Log.e(TAG, "Failed to convert response to OAuthResponse " + response);
            e.printStackTrace();
        }
        return null;
    }


    void updateUserInfo() {
        if (StringUtils.isEmpty(mUsername)) {
            return;
        }
        Request request = new Request.Builder()
                .url(String.format(Locale.ENGLISH, USER_DEETS_URL, mUsername))
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            Log.v(TAG, "Response from server : " + response);
            if (response != null && response.body() != null) {
                String jsonResponse = response.body().string();
                Log.i(TAG, "User info response " + jsonResponse);
                QUser user = convertResponseToQUser(jsonResponse);
                if (user != null) {
                    Log.i(TAG, "Successfully parsed user info "+user);
                    mQUserProcessor.onNext(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private QUser convertResponseToQUser(String response) {
        try {
            return new ObjectMapper().readValue(response, QUser.class);
        } catch (IOException e) {
            Log.e(TAG, "Failed to convert response to QUser " + response);
            e.printStackTrace();
        }
        return null;
    }
}
