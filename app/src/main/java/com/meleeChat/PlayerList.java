package com.meleeChat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import retrofit2.http.Query;

/**
 * Created by nadia on 2/19/16.
 */
public class PlayerList extends AppCompatActivity {
    private SharedPreferences settings;
    private static final String LOG_TAG = "CHAT_ACTIVITY";
    private String user_id;
    private String nickname;
    private float lat;
    private float lon;
    private List<ResultList> responses;
    private ArrayList<ListElement> aList;
    private MyAdapter aa;

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
        getSupportActionBar().setTitle("Player List");
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
        //user_id = "20xx";
        nickname = settings.getString("nickname", null);
        Bundle b = getIntent().getExtras();
        lat = b.getFloat("LAT");
        lon = b.getFloat("LON");
        if (user_id == null || nickname == null) {
            //good lord something has gone wrong
        }
        refresh(findViewById(R.id.chat));
    }

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
            //tv.setBackgroundColor(R.color.me);
            //tv2.setBackgroundColor(R.color.me);


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
}//end PlayerList class
