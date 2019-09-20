package com.dwarsoftgames.edufun.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dwarsoftgames.edufun.R;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.dwarsoftgames.edufun.UI.MainActivity.SHAREDPREF;
import static com.dwarsoftgames.edufun.Utils.UTCToIST;

public class QuizActivity extends AppCompatActivity {

    private String GET_QUIZ = "https://edufun.dwarsoft.com/api/edufun/Quizzes?ClassID=";

    private MaterialSpinner spClass;
    private RecyclerView rvQuiz;

    private ImageView ivBack;

    private int classID;

    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;
    private ArrayAdapter<String> spinnerAdapter_class;

    private final ArrayList<Integer> quizID = new ArrayList<>();
    private final ArrayList<String> subjectName = new ArrayList<>();
    private final ArrayList<String> quizName = new ArrayList<>();
    private final ArrayList<String> quizDescription = new ArrayList<>();
    private final ArrayList<String> quizStart = new ArrayList<>();
    private final ArrayList<String> quizEnd = new ArrayList<>();
    private final ArrayList<Integer> attempts = new ArrayList<>();
    private final ArrayList<Integer> success = new ArrayList<>();
    private final ArrayList<Integer> failure = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));

        setViews();
        initData();
        setSpinnerData();
        setOnClicks();
    }

    private void setViews() {
        spClass = findViewById(R.id.spClass);
        rvQuiz = findViewById(R.id.rvQuiz);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
    }

    private void setSpinnerData() {
        spClass.setHint("Select Year");
        spClass.setHintTextColor(getResources().getColor(R.color.black));

        spinnerAdapter_class = new ArrayAdapter<>(QuizActivity.this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter_class.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter_class.add("Select Class");
        spinnerAdapter_class.add("I - Year");
        spinnerAdapter_class.add("II - Year");
        spinnerAdapter_class.add("III - Year");
        spinnerAdapter_class.add("IV - Year");

        spClass.setAdapter(spinnerAdapter_class);
    }

    private void setOnClicks() {
        spClass.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (position == 0) {
                    Toast.makeText(QuizActivity.this, "Please select your year", Toast.LENGTH_SHORT).show();
                } else {
                    classID = position;
                    fetchQuizList();
                }
            }
        });
    }

    private void fetchQuizList() {
        requestQueue.add(requestQuizList());
    }

    private JsonObjectRequest requestQuizList() {

        quizID.clear();
        subjectName.clear();
        quizName.clear();
        quizDescription.clear();
        quizStart.clear();
        quizEnd.clear();
        attempts.clear();
        success.clear();
        failure.clear();

        String url = GET_QUIZ + classID;

        return new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        parseQuizData(response);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void parseQuizData(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {
                JSONArray jsonArray = jsonObject.getJSONArray("quizzes");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    quizID.add(jsonObject1.getInt("quizID"));
                    subjectName.add(getSubjectName(jsonObject1.getInt("subjectID")));
                    quizName.add(jsonObject1.getString("quizName"));
                    quizDescription.add(jsonObject1.getString("quizDescription"));
                    quizStart.add(UTCToIST(jsonObject1.getString("quizStart")));
                    quizEnd.add(UTCToIST(jsonObject1.getString("quizEnd")));
                    attempts.add(jsonObject1.getInt("attempts"));
                    success.add(jsonObject1.getInt("success"));
                    failure.add(jsonObject1.getInt("failure"));
                }
                rvQuiz.setHasFixedSize(true);
                QuizzesAdapter quizzesAdapter = new QuizzesAdapter(quizID, subjectName, quizName, quizDescription, quizStart, quizEnd,
                        attempts, success, failure);
                rvQuiz.setLayoutManager(new StaggeredGridLayoutManager(1,1));
                rvQuiz.setAdapter(quizzesAdapter);
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error 1", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error 2", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private String getSubjectName(int subjectID) {

        if (subjectID == 2) {
            return "Maths I";
        } else if (subjectID == 3) {
            return "Physics I";
        } else if (subjectID == 4) {
            return "PDS I";
        } else if (subjectID == 5) {
            return "DSP";
        } else {
            return "DBMS";
        }
    }

    public class QuizzesAdapter extends RecyclerView.Adapter<QuizzesAdapter.ViewHolder> {

        private final ArrayList<Integer> quizID_adapter;
        private final ArrayList<String> subjectName_adapter;
        private final ArrayList<String> quizName_adapter;
        private final ArrayList<String> quizDescription_adapter;
        private final ArrayList<String> quizStart_adapter;
        private final ArrayList<String> quizEnd_adapter;
        private final ArrayList<Integer> attempts_adapter;
        private final ArrayList<Integer> success_adapter;
        private final ArrayList<Integer> failure_adapter;

        private QuizzesAdapter(ArrayList<Integer> quizID, ArrayList<String> subjectName, ArrayList<String> quizName,
                               ArrayList<String> quizDescription, ArrayList<String> quizStart, ArrayList<String> quizEnd,
                               ArrayList<Integer> attempts, ArrayList<Integer> success, ArrayList<Integer> failure) {
            this.quizID_adapter = quizID;
            this.subjectName_adapter = subjectName;
            this.quizName_adapter = quizName;
            this.quizDescription_adapter = quizDescription;
            this.quizStart_adapter = quizStart;
            this.quizEnd_adapter = quizEnd;
            this.attempts_adapter = attempts;
            this.success_adapter = success;
            this.failure_adapter = failure;
        }

        @NonNull
        @Override
        public QuizzesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_quiz, parent, false);
            return new QuizzesAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final QuizzesAdapter.ViewHolder holder, int position) {

            String title = "Quiz  #" + (holder.getAdapterPosition()+1) + " - " + subjectName_adapter.get(holder.getAdapterPosition());
            
            holder.tvTitle.setText(title);
            holder.tvDescription.setText(quizDescription_adapter.get(holder.getAdapterPosition()));
            holder.tvStart.setText("Start Date - " + quizStart_adapter.get(holder.getAdapterPosition()));
            holder.tvEnd.setText("End Date - " + quizEnd_adapter.get(holder.getAdapterPosition()));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPreferences.edit().putInt("QuizDisplayID",(holder.getAdapterPosition()+1)).apply();
                    sharedPreferences.edit().putInt("QuizID",quizID_adapter.get(holder.getAdapterPosition())).apply();
                    sharedPreferences.edit().putString("QuizName",quizName_adapter.get(holder.getAdapterPosition())).apply();
                    sharedPreferences.edit().putInt("ClassID",classID).apply();

                    sharedPreferences.edit().putInt("attempts",attempts_adapter.get(holder.getAdapterPosition())).apply();
                    sharedPreferences.edit().putInt("success",success_adapter.get(holder.getAdapterPosition())).apply();
                    sharedPreferences.edit().putInt("failure",failure_adapter.get(holder.getAdapterPosition())).apply();

                    Intent intent = new Intent(QuizActivity.this, IndividualQuiz.class);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return quizName_adapter.size();
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
