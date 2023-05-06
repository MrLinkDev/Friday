package ru.linkstuff.friday.PhraseService;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

import ru.linkstuff.friday.Friday;
import ru.linkstuff.friday.HelperClasses.Weather.Weather;
import ru.linkstuff.friday.NewsFeed;
import ru.linkstuff.friday.R;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.PhraseSpotter;
import ru.yandex.speechkit.PhraseSpotterListener;
import ru.yandex.speechkit.PhraseSpotterModel;
import ru.yandex.speechkit.SpeechKit;

public class PhraseService extends Service implements PhraseSpotterListener {
    private static final String API_KEY = "6f29195e-0ac8-4979-ad8f-b70398be65b8";
    public static NotificationManager notification;
    private NotificationCompat.Builder builder;
    private Context context;
    private Location location;

    @Override
    public void onCreate() {
        super.onCreate();
        notification = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SpeechKit.getInstance().configure(context, API_KEY);
        PhraseSpotterModel model = new PhraseSpotterModel("phrase-spotter-model");
        Error loadResult = model.load();

        if (loadResult.getCode() == Error.ERROR_OK) {
            PhraseSpotter.setListener(this);
            PhraseSpotter.setModel(model);
        }
        start();

        return START_STICKY;
    }

    private Location getLocation() {
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;

        for (String provider : providers){
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) continue;
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) bestLocation = l;
        }

        return bestLocation;
    }

    private String getCityName(){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> address = null;

        if (location == null) location = getLocation();

        try {
            if (location != null) address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address.get(0).getLocality();
    }

    private void showNotification(){
        WeatherConnector weatherConnector = new WeatherConnector(getCityName(), location = getLocation(), new AsyncResponse() {
            @Override
            public void processFinished(Weather output) {
                builder = new NotificationCompat.Builder(context, "friday_notify_id")
                        .setColor(getColor(R.color.colorPrimary))
                        .setSmallIcon(R.drawable.friday_notify_icon)
                        .setContentTitle(output.getCity() + ":  " + output.getTemperature())
                        .setContentText(output.getHourlySummary())
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(output.getHourlySummary()))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                Intent intent = new Intent(context, NewsFeed.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(Friday.class);
                stackBuilder.addNextIntent(intent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("friday_notify_id", "FRIDAY", NotificationManager.IMPORTANCE_LOW);
                    notification.createNotificationChannel(channel);
                }

                notification.notify(1, builder.build());
            }
        });
        weatherConnector.execute();
    }

    private void start(){
        ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        PhraseSpotter.start();
    }

    public static void stop() {
        PhraseSpotter.stop();
        notification.cancelAll();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPhraseSpotted(String s, int i) {
        Intent fridayCommandRecognizer = new Intent(context, Friday.class);
        fridayCommandRecognizer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(fridayCommandRecognizer);
        stop();
    }

    @Override
    public void onPhraseSpotterStarted() {
        if (builder == null) showNotification();
    }

    @Override
    public void onPhraseSpotterStopped() {

    }

    @Override
    public void onPhraseSpotterError(Error error) {

    }

    @Override
    public void onPhraseSpotted(@NonNull PhraseSpotter phraseSpotter, @NonNull String s, int i) {

    }

    @Override
    public void onPhraseSpotterStarted(@NonNull PhraseSpotter phraseSpotter) {

    }

    @Override
    public void onPhraseSpotterError(@NonNull PhraseSpotter phraseSpotter, @NonNull Error error) {

    }

    private static class WeatherConnector extends AsyncTask<Void, Void, Void>{
        private AsyncResponse delegate;

        private final String API_KEY = "4e69c5a9b70409123132c0b7cc6c3ef3";
        private final String URL = "https://api.darksky.net/forecast/";

        private JSONObject jsonObject;
        private Location location;
        private Weather weather;
        private String cityName;

        WeatherConnector(String cityName, Location location, AsyncResponse delegate){
            this.cityName = cityName;
            this.location = location;
            this.delegate = delegate;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            weather = getWeather();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (weather != null){
                delegate.processFinished(weather);
            }
        }

        Weather getWeather() {

            try {
                jsonObject = new JSONObject(connectToURL());

                weather = new Weather(
                        cityName,
                        jsonObject.getJSONObject("currently").getString("summary"),
                        jsonObject.getJSONObject("currently").getDouble("temperature"),
                        jsonObject.getJSONObject("hourly").getString("summary")
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (weather != null){
                Log.d("WEATHER", weather.getCity());
                Log.d("WEATHER", weather.getSummary());
                Log.d("WEATHER", weather.getTemperature());
                Log.d("WEATHER", weather.getHourlySummary());
            }

            return weather;
        }

        private String connectToURL() {
            StringBuilder builder = new StringBuilder();
            String line;

            try {
                if (location != null){
                    java.net.URL url = new URL(URL + API_KEY + "/" + location.getLatitude() + "," + location.getLongitude() + "?" + "lang=ru");
                    URLConnection connection = url.openConnection();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = reader.readLine()) != null) builder.append(line).append("\n");
                    reader.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return builder.toString();
        }


    }

    interface AsyncResponse{
        void processFinished(Weather output);
    }
}

