package com.dwarsoftgames.edufun.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Map;
import java.util.Objects;

import static com.dwarsoftgames.edufun.UI.MainActivity.SHAREDPREF;

public class DoubtsActivity extends AppCompatActivity {

    private String POST_DOUBTS = "https://edufun.dwarsoft.com/api/edufun/PostDoubt";
    private String GET_DOUBTS = "https://edufun.dwarsoft.com/api/edufun/GetDoubts";

    private RecyclerView rvForum;
    private MaterialButton btDoubts;

    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    private int subjectID;
    private String doubt;

    private final ArrayList<String> subjectName = new ArrayList<>();
    private final ArrayList<String> question = new ArrayList<>();
    private final ArrayList<String> answer = new ArrayList<>();

    private ViewGroup viewGroup = null;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doubts);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));

        setViews();
        initData();
        setOnClicks();
    }

    private void setViews() {
        rvForum = findViewById(R.id.rvForum);
        btDoubts = findViewById(R.id.btDoubts);
    }

    private void initData() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);

        fetchDoubts();
    }

    private void setOnClicks() {
        btDoubts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRaiseDoubtsDialog();
            }
        });
    }

    private void showDoubtDialog() {

        new AlertDialog.Builder(DoubtsActivity.this)
                .setTitle("Raise Doubts")
                .setMessage("Are you sure you want to raise the doubt?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeAlert();
                        raiseDoubt();
                    }
                })
                .setNegativeButton("No",null)
                .show();
    }

    private void raiseDoubt() {

        int uid = sharedPreferences.getInt("userId",0);

        Map<String, String> params = new HashMap<>();
        params.put("userID", String.valueOf(uid));
        params.put("subjectID", String.valueOf(subjectID));
        params.put("question", doubt);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                POST_DOUBTS, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseResponse(response);
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

    private void parseResponse(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Doubt successfully raised", Snackbar.LENGTH_LONG);
                snackbar.show();

                fetchDoubts();
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void fetchDoubts() {
        requestQueue.add(requestDoubts());
    }

    private JsonObjectRequest requestDoubts() {

        subjectName.clear();
        question.clear();
        answer.clear();

        String url = GET_DOUBTS;

        return new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        parseDoubts(response);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void parseDoubts(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {
                JSONArray jsonArray = jsonObject.getJSONArray("doubts");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                    int subjectID = jsonObject1.getInt("subjectID");
                    String ans = jsonObject1.getString("answers");

                    subjectName.add(getSubjectName(subjectID));
                    question.add(jsonObject1.getString("question"));
                    if (ans.length() > 2) {
                        answer.add(ans);
                    } else {
                        answer.add("Not yet answered");
                    }
                }
                rvForum.setHasFixedSize(true);
                DoubtsAdapter DoubtsAdapter = new DoubtsAdapter(subjectName, question, answer);
                rvForum.setLayoutManager(new StaggeredGridLayoutManager(1,1));
                rvForum.setAdapter(DoubtsAdapter);
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

    public class DoubtsAdapter extends RecyclerView.Adapter<DoubtsAdapter.ViewHolder> {

        private final ArrayList<String> subjectName_adapter;
        private final ArrayList<String> question_adapter;
        private final ArrayList<String> answer_adapter;

        private DoubtsAdapter(ArrayList<String> subjectName, ArrayList<String> question, ArrayList<String> answer) {
            this.subjectName_adapter = subjectName;
            this.question_adapter = question;
            this.answer_adapter = answer;
        }

        @NonNull
        @Override
        public DoubtsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_doubts, parent, false);
            return new DoubtsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final DoubtsAdapter.ViewHolder holder, int position) {

            holder.tvName.setText(subjectName_adapter.get(holder.getAdapterPosition()));
            holder.tvQuestion.setText(question_adapter.get(holder.getAdapterPosition()));
            holder.tvAnswer.setText(answer_adapter.get(holder.getAdapterPosition()));

        }

        @Override
        public int getItemCount() {
            return subjectName_adapter.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final TextView tvName;
            final TextView tvQuestion;
            final TextView tvAnswer;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvQuestion = itemView.findViewById(R.id.tvQuestion);
                tvAnswer = itemView.findViewById(R.id.tvAnswer);
            }
        }
    }

    private void showRaiseDoubtsDialog() {
        AlertDialog.Builder builderr = new AlertDialog.Builder(DoubtsActivity.this);
        View view = LayoutInflater.from(DoubtsActivity.this).inflate(R.layout.custom_doubts_dialog,viewGroup,false);
        builderr.setView(view);
        builderr.setCancelable(false);

        ArrayAdapter<String> spinnerAdapter_doubts;
        final MaterialSpinner spSubjects;
        final EditText etDoubts;
        MaterialButton btPublish;

        spSubjects = view.findViewById(R.id.spSubjects);
        etDoubts = view.findViewById(R.id.etDoubts);
        btPublish = view.findViewById(R.id.btPublish);

        spSubjects.setHint("Select Subject");
        spSubjects.setHintTextColor(getResources().getColor(R.color.black));

        spinnerAdapter_doubts = new ArrayAdapter<>(DoubtsActivity.this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter_doubts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter_doubts.add("Select Subject");
        spinnerAdapter_doubts.add("Maths I");
        spinnerAdapter_doubts.add("Physics I");
        spinnerAdapter_doubts.add("PDS I");
        spinnerAdapter_doubts.add("DSP");
        spinnerAdapter_doubts.add("DBMS");

        spSubjects.setAdapter(spinnerAdapter_doubts);

        btPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subjectID = spSubjects.getSelectedIndex() + 1;
                doubt = etDoubts.getText().toString().trim();
                if (subjectID == 1) {
                    Toast.makeText(DoubtsActivity.this, "Please select the subject", Toast.LENGTH_SHORT).show();
                } else {
                    if (doubt.length() == 0) {
                        Toast.makeText(DoubtsActivity.this, "Please write description of the doubt", Toast.LENGTH_SHORT).show();
                    } else {
                        showDoubtDialog();
                    }
                }
            }
        });

        alertDialog = builderr.create();
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 100);
        Objects.requireNonNull(alertDialog.getWindow()).getAttributes().windowAnimations = R.style.Animation_WindowSlideUpDown;
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(inset);
        alertDialog.show();
    }

    private void closeAlert() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.cancel();
        }
    }
}