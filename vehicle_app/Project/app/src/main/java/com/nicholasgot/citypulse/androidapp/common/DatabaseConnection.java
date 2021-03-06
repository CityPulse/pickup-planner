package com.nicholasgot.citypulse.androidapp.common;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.nicholasgot.citypulse.androidapp.TravelPlannerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Connect to the database through web server
 */
public class DatabaseConnection {

    public static final String HOST = Configs.HOST_ADDRESS + ":" + Configs.PORT;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String LOG_TAG = DatabaseConnection.class.getSimpleName();

    public static String mResponseData;
    private Context mContext;

    public DatabaseConnection(Context context) {
        mContext = context;
    }

    /**
     * Return vehicle trips
     */
    // TODO: flag to indicate the trip has been served; update DB with flag once chosen
    public void getVehicleTrips() {

        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(HOST).newBuilder();
        urlBuilder.addPathSegment("vehicleapp")
                .addPathSegment("vehicle_trips");
        String okUrl = urlBuilder.build().toString();

        Log.v(LOG_TAG, "Formed url: " + okUrl);

        Request request = new Request.Builder()
                .url(okUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error connecting to the database");

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Error connecting to the database.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                else {
                    String rowsSelected = response.body().string();
                    mResponseData = rowsSelected;
                    Log.v(LOG_TAG, "DB response: " + rowsSelected);
                    TravelPlannerActivity.getLocationFromJson(rowsSelected);
                }
            }
        });
    }

    /**
     * POSTs to the webservice endpoint
     * @param source source lat/lon pair
     * @param destination destination lat/lon pair
     */
    public void postLocation(String source, String destination) {

        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(HOST).newBuilder();
        urlBuilder.addPathSegment("clientapp")
                .addPathSegment("requests");
        String okUrl = urlBuilder.build().toString();

        RequestBody formBody;
        try {
            JSONObject json = new JSONObject();
            json.put("source", source);
            json.put("destination", destination);
            formBody = RequestBody.create(JSON, json.toString());

        } catch (JSONException je) {
            formBody = new FormBody.Builder()
                    .add("source", source)
                    .add("destination", destination)
                    .build();

            Log.v(LOG_TAG, "Json Error: " + je);
        }

        Request request = new Request.Builder()
                .url(okUrl)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                else {
                    String rowsInserted = response.body().string();
//                    Log.v(LOG_TAG, "DB response: " + rowsInserted);
                }
            }
        });
    }
}

