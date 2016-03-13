package com.meleeChat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private static String username;
    private static String APIkey;
    private static final String LOG_TAG = "MAIN_ACTIVITY";
    private double prevAcc = (double) 1e10;
    private double lat;
    private double lon;
    private long prevAccTime = 0;
    Location prevLoc;

    private boolean getLoginInfo() {
        EditText editText = (EditText) findViewById(R.id.username);
        username = editText.getText().toString();

        editText = (EditText) findViewById(R.id.key);
        APIkey = editText.getText().toString();

        if (username == null || APIkey == null) {
            return false;
        }
        return true;
    }

    private boolean getLocationPermission(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return settings.getBoolean("location_allowed", false);
    }

    private void setLocationPermission(boolean b) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("location_allowed", b);
        editor.commit();
    }

    public void onClick(View v) {
        if (getLoginInfo() && getLocationPermission()) {
            //disable button
            //check loginInfo valid
            Intent intent = new Intent(this, TOMenu.class);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("APIkey", APIkey);
            editor.commit();
            editor.putString("username", username);
            editor.commit();
            Bundle b = new Bundle();
            b.putFloat("LAT", (float) lat);
            b.putFloat("LON", (float) lon);
            intent.putExtras(b);
            startActivity(intent);
            //new Feedback().execute("https://api.challonge.com/v1/tournaments.json");
        }
        else {
            //make a toast saying try again and fill out shit and wait for location
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String user_id;
        // Gets the settings, and creates a random user id if missing.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        user_id = settings.getString("user_id", null);
        if (user_id == null) {
            // Creates a random one, and sets it.
            SecureRandomString srs = new SecureRandomString();
            user_id = srs.nextString();
            SharedPreferences.Editor e = settings.edit();
            e.putString("user_id", user_id);
            e.commit();
        }
    }

    @Override
    protected  void onResume() {
        setLocationPermission(false);
        try {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        catch (SecurityException e) {

        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stops the location updates.
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.removeUpdates(locationListener);
        }
        catch (SecurityException e) {

        }

    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
            double newAcc = loc.getAccuracy();
            long newTime = loc.getTime();
            lat = loc.getLatitude();
            lon = loc.getLongitude();
            Log.i(LOG_TAG, "prevLoc: " + prevLoc + "\tprevAcc: " + prevAcc + "\tprevAccTime: " + prevAccTime);
            //Log.i(LOG_TAG, "Accuracy is " + newAcc);

            boolean isBetter = ((prevLoc == null) ||
                    newAcc < prevAcc + (newTime - prevAccTime));

            if (isBetter) {
                prevLoc = loc;
                prevAcc = loc.getAccuracy();
                prevAccTime = loc.getTime();
            }

            if (newAcc <= 50) {
                setLocationPermission(true);
            } else {
                setLocationPermission(false);
            }
            Button button = (Button) findViewById(R.id.start_chat);
            if (getLocationPermission()) {
                button.setText("Start Chat");
            } else {
                button.setText("");
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };



    /*
    private class Feedback extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection urlConnection;
            String userpass = username + ":" + APIkey;
            String result = "";
            // do above Server call here
            try
            {
                URL url = new URL("https://api.challonge.com/v1/tournaments.json?subdomain=smashing121");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(userpass.getBytes(), Base64.NO_WRAP)));
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Log.i("Code is:", ""+urlConnection.getResponseCode());
                if (in != null)
                {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    Log.i("Information is", ""+bufferedReader.readLine());

                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }

                }

                in.close();
                urlConnection.disconnect();
                return result;
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return "some message";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }
    */
}