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

public class AssignmentActivity extends AppCompatActivity {

    private String ASSIGNMENTS = "https://edufun.dwarsoft.com/api/edufun/Assignments";

    private ImageView ivBack;
    private RecyclerView rvAssignments;

    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    private final ArrayList<String> assignmentID = new ArrayList<>();
    private final ArrayList<String> assignmentTitle = new ArrayList<>();
    private final ArrayList<Integer> assignmentScore = new ArrayList<>();
    private final ArrayList<String> assignmentStart = new ArrayList<>();
    private final ArrayList<String> assignmentEnd = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

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
        rvAssignments = findViewById(R.id.rvAssignments);
    }

    private void initData() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);

        fetchAssignments();
    }

    private void setOnClicks() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void fetchAssignments() {
        requestQueue.add(requestAssignments());
    }

    private JsonObjectRequest requestAssignments() {

        assignmentID.clear();
        assignmentTitle.clear();
        assignmentScore.clear();
        assignmentStart.clear();
        assignmentEnd.clear();

        String url = ASSIGNMENTS;

        return new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        parseAssignments(response);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void parseAssignments(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {
                JSONArray jsonArray = jsonObject.getJSONArray("assignments");
                int j = 1;
                for (int i = jsonArray.length() - 1; i >= 0; i--) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    assignmentID.add("Assignment #"+j);
                    assignmentTitle.add(jsonObject1.getString("assignmentTitle"));
                    assignmentScore.add(jsonObject1.getInt("assignmentScore"));
                    assignmentStart.add(UTCToIST(jsonObject1.getString("assignmentStart")));
                    assignmentEnd.add(UTCToIST(jsonObject1.getString("assignmentEnd")));
                    j++;
                }
                rvAssignments.setHasFixedSize(true);
                AssignmentAdapter assignmentAdapter = new AssignmentAdapter(assignmentID, assignmentTitle, assignmentScore,
                        assignmentStart, assignmentEnd);
                rvAssignments.setLayoutManager(new StaggeredGridLayoutManager(1,1));
                rvAssignments.setAdapter(assignmentAdapter);
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error 1", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error 2", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

        private final ArrayList<String> assignmentID_adapter;
        private final ArrayList<String> assignmentTitle_adapter;
        private final ArrayList<Integer> assignmentScore_adapter;
        private final ArrayList<String> assignmentStart_adapter;
        private final ArrayList<String> assignmentEnd_adapter;

        private AssignmentAdapter(ArrayList<String> assignmentID, ArrayList<String> assignmentTitle, ArrayList<Integer> assignmentScore,
                                  ArrayList<String> assignmentStart, ArrayList<String> assignmentEnd) {
            this.assignmentID_adapter = assignmentID;
            this.assignmentTitle_adapter = assignmentTitle;
            this.assignmentScore_adapter = assignmentScore;
            this.assignmentStart_adapter = assignmentStart;
            this.assignmentEnd_adapter = assignmentEnd;
        }

        @NonNull
        @Override
        public AssignmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_assignment, parent, false);
            return new AssignmentAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AssignmentAdapter.ViewHolder holder, int position) {

            String description = assignmentTitle_adapter.get(position).replaceAll("newline","\n");

            holder.tvTitle.setText(assignmentID_adapter.get(holder.getAdapterPosition()));
            holder.tvDescription.setText(description);
            holder.tvStart.setText(assignmentStart_adapter.get(holder.getAdapterPosition()));
            holder.tvEnd.setText(assignmentEnd_adapter.get(holder.getAdapterPosition()));

        }

        @Override
        public int getItemCount() {
            return assignmentID_adapter.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final TextView tvTitle;
            final TextView tvDescription;
            final TextView tvStart;
            final TextView tvEnd;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvStart = itemView.findViewById(R.id.tvStart);
                tvEnd = itemView.findViewById(R.id.tvEnd);
            }
        }
    }
}
