package com.dwarsoftgames.edufun.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dwarsoftgames.edufun.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.dwarsoftgames.edufun.UI.MainActivity.SHAREDPREF;
import static com.dwarsoftgames.edufun.Utils.UTCToIST;

public class IndividualQuiz extends AppCompatActivity {

    private String GET_LEADERBOARD = "https://edufun.dwarsoft.com/api/edufun/GetLeaderboard?QuizID=";
    private String START_QUIZ = "https://edufun.dwarsoft.com/api/edufun/StartQuiz";
    private String FETCH_QUESTIONS = "https://edufun.dwarsoft.com/api/edufun/Questions?QuizID=";

    private TextView tvDisplayID, tvName;
    private LinearLayout llQuiz;
    private RecyclerView rvLeaderboard;
    private TextView tvNoLeaderboard;
    private MaterialButton btQuiz, btStats;

    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;

    private final ArrayList<String> userName = new ArrayList<>();
    private final ArrayList<Integer> score = new ArrayList<>();

    private TextView tvAttempts, tvSuccess, tvFailure;
    private MaterialButton btPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_quiz);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));

        setViews();
        initData();
        setOnClicks();
        showQuiz();
    }

    private void setViews() {
        tvDisplayID = findViewById(R.id.tvDisplayID);
        tvName = findViewById(R.id.tvName);
        llQuiz = findViewById(R.id.llQuiz);
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        tvNoLeaderboard = findViewById(R.id.tvNoLeaderboard);
        btQuiz = findViewById(R.id.btQuiz);
        btStats = findViewById(R.id.btStats);

        tvAttempts = findViewById(R.id.tvAttempts);
        tvSuccess = findViewById(R.id.tvSuccess);
        tvFailure = findViewById(R.id.tvFailure);
        btPlay = findViewById(R.id.btPlay);
    }

    private void initData() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);

        String displayID = "Quiz #"+sharedPreferences.getInt("QuizDisplayID",0);
        tvDisplayID.setText(displayID);
        tvName.setText(sharedPreferences.getString("QuizName",""));
        tvAttempts.setText(String.valueOf(sharedPreferences.getInt("attempts",0)));
        tvSuccess.setText(String.valueOf(sharedPreferences.getInt("success",0)));
        tvFailure.setText(String.valueOf(sharedPreferences.getInt("failure",0)));
    }

    private void setOnClicks() {
        btQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuiz();
            }
        });

        btStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLeaderboard();
            }
        });

        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz();
            }
        });
    }

    private void showQuiz() {
        llQuiz.setVisibility(View.VISIBLE);
        rvLeaderboard.setVisibility(View.GONE);
        tvNoLeaderboard.setVisibility(View.GONE);
    }

    private void showLeaderboard() {
        llQuiz.setVisibility(View.GONE);
        rvLeaderboard.setVisibility(View.VISIBLE);
        tvNoLeaderboard.setVisibility(View.GONE);
        fetchLeaderboard();
    }

    private void fetchLeaderboard() {
        requestQueue.add(requestLeaderboard());
    }

    private JsonObjectRequest requestLeaderboard() {

        userName.clear();
        score.clear();

        String url = GET_LEADERBOARD + sharedPreferences.getInt("QuizID",0);

        return new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        parseLeaderboard(response);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void parseLeaderboard(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {
                JSONArray jsonArray = jsonObject.getJSONArray("leaderboardDatas");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    userName.add(jsonObject1.getString("userName"));
                    score.add(jsonObject1.getInt("score"));
                }

                if (jsonArray.length() > 0) {
                    rvLeaderboard.setHasFixedSize(true);
                    LeaderboardAdapter LeaderboardAdapter = new LeaderboardAdapter(userName, score);
                    rvLeaderboard.setLayoutManager(new StaggeredGridLayoutManager(1,1));
                    rvLeaderboard.setAdapter(LeaderboardAdapter);
                } else {
                    tvNoLeaderboard.setVisibility(View.VISIBLE);
                    rvLeaderboard.setVisibility(View.GONE);
                }
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error 1", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error 2", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

        private final ArrayList<String> userName_adapter;
        private final ArrayList<Integer> score_adapter;

        private LeaderboardAdapter(ArrayList<String> userName, ArrayList<Integer> score) {
            this.userName_adapter = userName;
            this.score_adapter = score;
        }

        @NonNull
        @Override
        public LeaderboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_leaderboard, parent, false);
            return new LeaderboardAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final LeaderboardAdapter.ViewHolder holder, int position) {

            holder.tvLeaderboardName.setText(userName_adapter.get(holder.getAdapterPosition()));
            holder.tvLeaderboardScore.setText(String.valueOf(score_adapter.get(holder.getAdapterPosition())));
        }

        @Override
        public int getItemCount() {
            return userName_adapter.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final TextView tvLeaderboardName;
            final TextView tvLeaderboardScore;

            ViewHolder(View itemView) {
                super(itemView);
                tvLeaderboardName = itemView.findViewById(R.id.tvLeaderboardName);
                tvLeaderboardScore = itemView.findViewById(R.id.tvLeaderboardScore);
            }
        }
    }

    private void startQuiz() {

        int uid = sharedPreferences.getInt("userId",0);

        Map<String, String> params = new HashMap<>();
        params.put("userID", String.valueOf(uid));
        params.put("quizID", String.valueOf(sharedPreferences.getInt("QuizID",0)));

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                START_QUIZ, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseStartQuiz(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        jsonObjReq.setTag("DoubtsRequest");
        requestQueue.add(jsonObjReq);
    }

    private void parseStartQuiz(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Starting Quiz", Snackbar.LENGTH_LONG);
                snackbar.show();
                fetchQuestions();
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),jsonObject.getString("errorMessage"), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void fetchQuestions() {
        requestQueue.add(requestQuestions());
    }

    private JsonObjectRequest requestQuestions() {

        String url = FETCH_QUESTIONS + sharedPreferences.getInt("QuizID",0);

        return new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        parseQuestions(response);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void parseQuestions(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {
                Set<String> questionSet = new HashSet<String>();
                Set<String> optionA = new HashSet<String>();
                Set<String> optionB = new HashSet<String>();
                Set<String> optionC = new HashSet<String>();
                Set<String> optionD = new HashSet<String>();
                Set<String> correctAnswer = new HashSet<String>();

                JSONArray jsonArray = jsonObject.getJSONArray("questions");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    questionSet.add(jsonObject1.getString("question"));
                    optionA.add(jsonObject1.getString("optionA"));
                    optionB.add(jsonObject1.getString("optionB"));
                    optionC.add(jsonObject1.getString("optionC"));
                    optionD.add(jsonObject1.getString("optionD"));
                    correctAnswer.add(i+jsonObject1.getString("correctAnswer"));
                }
                sharedPreferences.edit().putStringSet("questionSet",questionSet).apply();
                sharedPreferences.edit().putStringSet("optionA",optionA).apply();
                sharedPreferences.edit().putStringSet("optionB",optionB).apply();
                sharedPreferences.edit().putStringSet("optionC",optionC).apply();
                sharedPreferences.edit().putStringSet("optionD",optionD).apply();
                sharedPreferences.edit().putStringSet("correctAnswer",correctAnswer).apply();

                Intent intent = new Intent(IndividualQuiz.this,GamePlay.class);
                startActivity(intent);
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error 1", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error 2", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
}
