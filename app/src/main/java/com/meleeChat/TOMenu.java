package com.meleeChat;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.util.Base64;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by nadia on 3/12/16.
 */
public class TOMenu extends AppCompatActivity {

    private SharedPreferences settings;
    private static final String LOG_TAG = "CHAT_ACTIVITY";
    private String user_id;
    private String username;
    private String APIkey;
    private float lat;
    private float lon;
    private List<ResultList> responses;
    private ArrayList<ListElement> aList;
    //private MyAdapter aa;

    private class ListElement {
        ListElement(String tl, String bl, String x) {
            content = tl;
            user = bl;
            id = x;
        }

        public String content;
        public String user;
        public String id;
    }


    public void sendMessage(String message) {
        //Magic HTTP stuff
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://luca-teaching.appspot.com/localmessages/default/")
                .addConverterFactory(GsonConverterFactory.create())    //parse Gson string
                .client(httpClient)    //add logging
                .build();

        MessageService service = retrofit.create(MessageService.class);


        //final EditText editText = (EditText) findViewById(R.id.message_box);

        System.out.println("MESSAGE: " + message);
        if (!message.equals("")) {
            SecureRandomString srs = new SecureRandomString();
            String message_id = srs.nextString();

            Call<Messages> queryResponseCall =
                    service.post_Message(lat, lon, username, user_id, message, message_id);


            //Call retrofit asynchronously
            queryResponseCall.enqueue(new Callback<Messages>() {
                @Override
                public void onResponse(Response<Messages> response) {
                    if (response.code() == 200) {
                    //(response.body().result.equals("ok") &&
                    Log.i(LOG_TAG, "Code is: " + response.code());
                    Log.i(LOG_TAG, "The result is: " + response.body().result);
                    }
                    else {
                        Log.i(LOG_TAG, "Code is: " + response.code());
                    }
                    //editText.setText("");
                    //refresh(findViewById(R.id.chat));
                }

                @Override
                public void onFailure(Throwable t) {
                    // Log error here since request failed
                }
            });
        }
    }

    /*
    public void refresh(View v) {
        //Magic HTTP stuff
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://luca-teaching.appspot.com/localmessages/default/")
                .addConverterFactory(GsonConverterFactory.create())    //parse Gson string
                .client(httpClient)    //add logging
                .build();

        MessageService service = retrofit.create(MessageService.class);

        Call<Messages> queryResponseCall =
                service.get_Messages(lat, lon, user_id);

        //Call retrofit asynchronously
        queryResponseCall.enqueue(new Callback<Messages>() {
            @Override
            public void onResponse(Response<Messages> response) {
                if (response.body().result.equals("ok") && response.code() == 200) {

                    Log.i(LOG_TAG, "Code is: " + response.code());
                    Log.i(LOG_TAG, "The result is: " + response.body().result);
                    Log.i(LOG_TAG, "resultList: " + response.body().resultList);

                    responses = response.body().resultList;

                    for (int i = 0; i < responses.size(); i++) {
                        Log.i(LOG_TAG, "messages: " + responses.get(i).message);
                    }
                    populateList();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }
        });
    }

    private void populateList() {
        aList = new ArrayList<ListElement>();
        aa = new MyAdapter(this, R.layout.player_info, aList);
        ListView myListView = (ListView) findViewById(R.id.player_list);

        for (int i = (responses.size() - 1); i >= 0; i--) {
            //System.out.println("SIZE: " + i);
            ListElement le = new ListElement(responses.get(i).message, responses.get(i).nickname, responses.get(i).userId);
            myListView.setAdapter(aa);
            aList.add(le);
        }
        aa.notifyDataSetChanged();
    }
    */

    public interface MessageService {
        @GET("post_message")
        Call<Messages> post_Message(@Query("lat") float lat,
                                    @Query("lng") float lng,
                                    @Query("nickname") String nickname,
                                    @Query("user_id") String user_id,
                                    @Query("message") String message,
                                     @Query("message_id") String message_id);

        @GET("get_messages")
        Call<Messages> get_Messages(@Query("lat") float lat,
                                    @Query("lng") float lng,
                                    @Query("user_id") String user_id);
    }


    @Override
    protected void onResume() {
        getSupportActionBar().setTitle("Tournament List");
        Bundle b = getIntent().getExtras();
        lat = b.getFloat("LAT");
        lon = b.getFloat("LON");


        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        user_id = settings.getString("user_id", null);
        username = settings.getString("username", null);
        System.out.println("USERNAME: " +username);
        APIkey = settings.getString("APIkey", null);
        Bundle b = getIntent().getExtras();
        lat = b.getFloat("LAT");
        lon = b.getFloat("LON");
        if (user_id.equals(null) || username.equals(null)) {
            //good lord something has gone wrong
            System.out.println("THINGS ARE NULL OH NO");
        }
        new Feedback().execute("https://api.challonge.com/v1/tournaments.json");
        //refresh(findViewById(R.id.chat));
    }

    /*
    private class MyAdapter extends ArrayAdapter<ListElement> {
        private int resource;
        private Context context;

        public MyAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            ListElement w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fills in the view.
            TextView tv = (TextView) newView.findViewById(R.id.player_tag);
            TextView tv2 = (TextView) newView.findViewById(R.id.player_name);
            tv.setText(w.content);
            tv2.setText(w.user);



            System.out.println("w.id: " + w.id + "\nuser_id: " + user_id);
            if (w.id.equals(user_id)) {
                tv.setGravity(5); //align right
                tv2.setGravity(5);
            }
            else {
                //tv.setBackgroundColor(R.color.friend);
                //tv2.setBackgroundColor(R.color.friend);
                tv.setGravity(3); //align left
                tv2.setGravity(3);
            }

            return newView;
        }//end getView

    } //end MyAdapter class
    */

    private class Feedback extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection urlConnection;
            String userpass = username + ":" + APIkey;
            System.out.println("USERPASS: " + userpass);
            String result = "";
            // do above Server call here
            try
            {
                URL url = new URL("https://api.challonge.com/v1/tournaments.json?subdomain=smashing121");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Basic " + new String(Base64.encode(userpass.getBytes(), Base64.NO_WRAP)));
                BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Log.i("Code is:", "" + urlConnection.getResponseCode());
                if (in != null)
                {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = bufferedReader.readLine();
                    Log.i("Information is", "" + line);

                    sendMessage(line); //send tournament info as JSON to server so others can read

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
}
