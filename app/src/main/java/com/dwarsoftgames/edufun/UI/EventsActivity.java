package com.dwarsoftgames.edufun.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dwarsoftgames.edufun.R;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.dwarsoftgames.edufun.UI.MainActivity.SHAREDPREF;
import static com.dwarsoftgames.edufun.Utils.UTCToIST;

public class EventsActivity extends AppCompatActivity {

    private String EVENTS = "https://edufun.dwarsoft.com/api/edufun/Events?DeptID=";

    private ImageView ivBack;
    private RecyclerView rvEvents;

    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    private final ArrayList<String> eventName = new ArrayList<>();
    private final ArrayList<String> eventDescription = new ArrayList<>();
    private final ArrayList<String> eventDate = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));

        setViews();
        initData();
        setOnClicks();
    }

    private void setViews() {
        ivBack = findViewById(R.id.ivBack);
        rvEvents = findViewById(R.id.rvEvents);
    }

    private void initData() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);

        fetchEvents();
    }

    private void setOnClicks() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void fetchEvents() {
        requestQueue.add(requestEvents());
    }

    private JsonObjectRequest requestEvents() {

        eventName.clear();
        eventDescription.clear();
        eventDate.clear();

        int DeptID = sharedPreferences.getInt("DeptID",0);
        String url = EVENTS + DeptID;

        return new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        parseEvents(response);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void parseEvents(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {
                JSONArray jsonArray = jsonObject.getJSONArray("events");
                for (int i = 0; i < jsonObject.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    eventName.add(jsonObject1.getString("eventName"));
                    eventDescription.add(jsonObject1.getString("eventDescription"));
                    eventDate.add(UTCToIST(jsonObject1.getString("eventDate")));
                }
                rvEvents.setHasFixedSize(true);
                EventsAdapter eventsAdapter = new EventsAdapter(eventName, eventDescription, eventDate);
                rvEvents.setLayoutManager(new StaggeredGridLayoutManager(1,1));
                rvEvents.setAdapter(eventsAdapter);
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error 1", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error 2", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

        private final ArrayList<String> eventName_adapter;
        private final ArrayList<String> eventDescription_adapter;
        private final ArrayList<String> eventDate_adapter;

        private EventsAdapter(ArrayList<String> eventName, ArrayList<String> eventDescription, ArrayList<String> eventDate) {
            this.eventName_adapter = eventName;
            this.eventDescription_adapter = eventDescription;
            this.eventDate_adapter = eventDate;
        }

        @NonNull
        @Override
        public EventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_assignment, parent, false);
            return new EventsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final EventsAdapter.ViewHolder holder, int position) {

            String description = eventDescription_adapter.get(position).replaceAll("newline","\n");

            holder.tvTitle.setText(eventName_adapter.get(holder.getAdapterPosition()));
            holder.tvDescription.setText(description);
            holder.tvStart.setText(eventDate_adapter.get(holder.getAdapterPosition()));

        }

        @Override
        public int getItemCount() {
            return eventName_adapter.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final TextView tvTitle;
            final TextView tvDescription;
            final TextView tvStart;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvStart = itemView.findViewById(R.id.tvStart);
            }
        }
    }
}
