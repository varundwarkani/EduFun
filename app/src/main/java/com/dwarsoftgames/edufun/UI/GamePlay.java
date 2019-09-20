package com.dwarsoftgames.edufun.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dwarsoftgames.edufun.R;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.dwarsoftgames.edufun.UI.MainActivity.SHAREDPREF;

public class GamePlay extends AppCompatActivity {

    private String POST_SCORE = "https://edufun.dwarsoft.com/api/edufun/PostScore";

    private CircleImageView tvQues1, tvQues2, tvQues3, tvQues4, tvQues5, tvQues6, tvQues7, tvQues8;
    private TextView tvQues, tvOp1, tvOp2, tvOp3, tvOp4;

    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;

    private Set<String> questionSet;
    private Set<String> optionASet;
    private Set<String> optionBSet;
    private Set<String> optionCSet;
    private Set<String> optionDSet;
    private Set<String> correctAnswerSet;

    private ArrayList<String> questions  = new ArrayList<>();
    private ArrayList<String> optionA  = new ArrayList<>();
    private ArrayList<String> optionB  = new ArrayList<>();
    private ArrayList<String> optionC  = new ArrayList<>();
    private ArrayList<String> optionD  = new ArrayList<>();
    private ArrayList<String> correctAnswer  = new ArrayList<>();

    private int questionCount;
    private int points;

    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.dark_blue));

        setViews();
        initData();
        setOnClicks();
    }

    private void setViews() {
        tvQues1 = findViewById(R.id.tvQues1);
        tvQues2 = findViewById(R.id.tvQues2);
        tvQues3 = findViewById(R.id.tvQues3);
        tvQues4 = findViewById(R.id.tvQues4);
        tvQues5 = findViewById(R.id.tvQues5);
        tvQues6 = findViewById(R.id.tvQues6);
        tvQues7 = findViewById(R.id.tvQues7);
        tvQues8 = findViewById(R.id.tvQues8);

        tvQues = findViewById(R.id.tvQues);
        tvOp1 = findViewById(R.id.tvOp1);
        tvOp2 = findViewById(R.id.tvOp2);
        tvOp3 = findViewById(R.id.tvOp3);
        tvOp4 = findViewById(R.id.tvOp4);
    }

    private void initData() {
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        questionSet = sharedPreferences.getStringSet("questionSet", null);
        optionASet = sharedPreferences.getStringSet("optionA", null);
        optionBSet = sharedPreferences.getStringSet("optionB", null);
        optionCSet = sharedPreferences.getStringSet("optionC", null);
        optionDSet = sharedPreferences.getStringSet("optionD", null);
        correctAnswerSet = sharedPreferences.getStringSet("correctAnswer",null);

        questionCount = 0;
        points = 0;

        questions.clear();
        optionA.clear();
        optionB.clear();
        optionC.clear();
        optionD.clear();
        correctAnswer.clear();

        questions.addAll(questionSet);
        optionA.addAll(optionASet);
        optionB.addAll(optionBSet);
        optionC.addAll(optionCSet);
        optionD.addAll(optionDSet);
        correctAnswer.addAll(correctAnswerSet);

        setQuestions();
    }

    private void setOnClicks() {
        tvOp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption(1);
            }
        });

        tvOp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption(2);
            }
        });

        tvOp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption(3);
            }
        });

        tvOp4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption(4);
            }
        });
    }

    private void selectedOption(int index) {
        int correct = 0;
        int gameEnd = 0;

        String correctans = correctAnswer.get(questionCount);
        int correctAnswerInteger = Integer.parseInt(correctans.substring(1,2));

        if (correctAnswerInteger == index) {
            points = points + 10;
            correct = 1;
        }
        if (questionCount == 0) {
            if (correct == 1) {
                //correct answer
                tvQues1.setImageDrawable(getResources().getDrawable(R.drawable.correct_answer));
            } else {
                //wrong answer
                tvQues1.setImageDrawable(getResources().getDrawable(R.drawable.wrong_answer));
            }
        } else if (questionCount == 1) {
            if (correct == 1) {
                //correct answer
                tvQues2.setImageDrawable(getResources().getDrawable(R.drawable.correct_answer));
            } else {
                //wrong answer
                tvQues2.setImageDrawable(getResources().getDrawable(R.drawable.wrong_answer));
            }
        } else if (questionCount == 2) {
            if (correct == 1) {
                //correct answer
                tvQues3.setImageDrawable(getResources().getDrawable(R.drawable.correct_answer));
            } else {
                //wrong answer
                tvQues3.setImageDrawable(getResources().getDrawable(R.drawable.wrong_answer));
            }
        } else if (questionCount == 3) {
            if (correct == 1) {
                //correct answer
                tvQues4.setImageDrawable(getResources().getDrawable(R.drawable.correct_answer));
            } else {
                //wrong answer
                tvQues4.setImageDrawable(getResources().getDrawable(R.drawable.wrong_answer));
            }
        } else if (questionCount == 4) {
            if (correct == 1) {
                //correct answer
                tvQues5.setImageDrawable(getResources().getDrawable(R.drawable.correct_answer));
            } else {
                //wrong answer
                tvQues5.setImageDrawable(getResources().getDrawable(R.drawable.wrong_answer));
            }
        } else if (questionCount == 5) {
            if (correct == 1) {
                //correct answer
                tvQues6.setImageDrawable(getResources().getDrawable(R.drawable.correct_answer));
            } else {
                //wrong answer
                tvQues6.setImageDrawable(getResources().getDrawable(R.drawable.wrong_answer));
            }
        } else if (questionCount == 6) {
            if (correct == 1) {
                //correct answer
                tvQues7.setImageDrawable(getResources().getDrawable(R.drawable.correct_answer));
            } else {
                //wrong answer
                tvQues7.setImageDrawable(getResources().getDrawable(R.drawable.wrong_answer));
            }
        } else if (questionCount == 7) {
            if (correct == 1) {
                //correct answer
                tvQues8.setImageDrawable(getResources().getDrawable(R.drawable.correct_answer));
            } else {
                //wrong answer
                tvQues8.setImageDrawable(getResources().getDrawable(R.drawable.wrong_answer));
            }
        }
        questionCount++;

        if (questionCount == 8) {
            gameEnd = 1;
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Game Over! Updating Scores...", Snackbar.LENGTH_SHORT);
            snackbar.show();
            disableOptions();
            showResultCard();
            postScore();
        }

        if (gameEnd != 1) {
            if (correct == 1) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Correct Answer! Next Question in 2 seconds...", Snackbar.LENGTH_SHORT);
                snackbar.show();
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Wrong Answer! Next Question in 2 seconds...", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }

            disableOptions();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    enableOptions();
                    setQuestions();
                }
            }, 2000);
        }
    }

    private void disableOptions() {
        tvOp1.setEnabled(false);
        tvOp2.setEnabled(false);
        tvOp3.setEnabled(false);
        tvOp4.setEnabled(false);
    }

    private void enableOptions() {
        tvOp1.setEnabled(true);
        tvOp2.setEnabled(true);
        tvOp3.setEnabled(true);
        tvOp4.setEnabled(true);
    }

    private void setQuestions() {
        tvQues.setText(questions.get(questionCount));
        tvOp1.setText(optionA.get(questionCount));
        tvOp2.setText(optionB.get(questionCount));
        tvOp3.setText(optionC.get(questionCount));
        tvOp4.setText(optionD.get(questionCount));
    }

    private void postScore() {

        int uid = sharedPreferences.getInt("userId",0);

        Map<String, String> params = new HashMap<>();
        params.put("userID", String.valueOf(uid));
        params.put("quizID", String.valueOf(sharedPreferences.getInt("QuizID",0)));
        params.put("score", String.valueOf(points));

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                POST_SCORE, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseAuth(response);
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

        jsonObjReq.setTag("LoginRequest");
        requestQueue.add(jsonObjReq);
    }

    private void parseAuth(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Scores Updated !", Snackbar.LENGTH_LONG);
                snackbar.show();
                showResultCard();
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void showResultCard() {
        LinearLayout llQues = findViewById(R.id.llQues);
        llQues.setVisibility(View.GONE);

        tvQues.setVisibility(View.GONE);
        tvOp1.setVisibility(View.GONE);
        tvOp2.setVisibility(View.GONE);
        tvOp3.setVisibility(View.GONE);
        tvOp4.setVisibility(View.GONE);

        //show result card
        View resultCard = findViewById(R.id.resultCard);
        ImageView ivTrophy = findViewById(R.id.ivTrophy);
        TextView tvCongrats = findViewById(R.id.tvCongrats);
        TextView tvWinnerMessage = findViewById(R.id.tvWinnerMessage);

        resultCard.setVisibility(View.VISIBLE);
        ivTrophy.setVisibility(View.VISIBLE);
        tvCongrats.setVisibility(View.VISIBLE);
        tvWinnerMessage.setVisibility(View.VISIBLE);

        if (mediaPlayer!=null)
        {
            mediaPlayer.reset();
            if (mediaPlayer.isPlaying())
            {
                mediaPlayer.stop();
            }
        }

        if (points < 50) {
            tvCongrats.setText("Oops!");
            tvWinnerMessage.setText("Score more than 50 to earn points");

            LottieAnimationView lottieAnimation = findViewById(R.id.lottieAnimationFail);
            lottieAnimation.setVisibility(View.VISIBLE);
            lottieAnimation.bringToFront();
            lottieAnimation.setAnimation("wrong.json");
            lottieAnimation.playAnimation();

            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.game_lost);

        } else {
            tvCongrats.setText("Congrats!");
            tvWinnerMessage.setText("You won points!");

            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.game_win);

            LottieAnimationView lottieAnimation = findViewById(R.id.lottieAnimation);
            lottieAnimation.setVisibility(View.VISIBLE);
            lottieAnimation.bringToFront();
            lottieAnimation.setAnimation("fireworks.json");
            lottieAnimation.playAnimation();
        }

        mediaPlayer.start();
    }
}