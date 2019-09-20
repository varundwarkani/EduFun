package com.dwarsoftgames.edufun.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.dwarsoftgames.edufun.UI.MainActivity.SHAREDPREF;
import static com.dwarsoftgames.edufun.Utils.UTCToIST;

public class RedeemActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private int coins;
    private int type;

    private TextView tvCoins;
    private TextView odRedeem, marksRedeem, attendanceRedeem;
    private MaterialButton btRedeem;
    private RecyclerView rvRedeem;
    private TextView tvRedeem;
    private ImageView ivBack;

    private RequestQueue requestQueue;

    private final ArrayList<Integer> redeemType = new ArrayList<>();
    private final ArrayList<String> redeemDate = new ArrayList<>();

    private String REDEEM = "https://edufun.dwarsoft.com/api/edufun/Redeem";
    private String REDEEM_DETAILS = "https://edufun.dwarsoft.com/api/edufun/RedeemHistory?UserID=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));

        setViews();
        initData();
        setOnClicks();
    }

    private void setViews() {
        tvCoins = findViewById(R.id.tvCoins);
        odRedeem = findViewById(R.id.odRedeem);
        marksRedeem = findViewById(R.id.marksRedeem);
        attendanceRedeem = findViewById(R.id.attendanceRedeem);
        btRedeem = findViewById(R.id.btRedeem);
        rvRedeem = findViewById(R.id.rvRedeem);
        tvRedeem = findViewById(R.id.tvRedeem);
        ivBack = findViewById(R.id.ivBack);
    }

    private void initData() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);

        coins = sharedPreferences.getInt("coins",0);
        tvCoins.setText(String.valueOf(coins));

        setRedeemData();
    }

    private void setOnClicks() {
        odRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coins >= 100) {
                    setRedeemSelected(1);
                } else {
                    Toast.makeText(RedeemActivity.this, "Not enough coins", Toast.LENGTH_SHORT).show();
                }
            }
        });

        marksRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coins >= 200) {
                    setRedeemSelected(2);
                } else {
                    Toast.makeText(RedeemActivity.this, "Not enough coins", Toast.LENGTH_SHORT).show();
                }
            }
        });

        attendanceRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coins >= 300) {
                    setRedeemSelected(3);
                } else {
                    Toast.makeText(RedeemActivity.this, "Not enough coins", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRedeemDialog();
            }
        });
    }

    private void setRedeemSelected(int selectedType) {
        type = selectedType;
        switch (selectedType) {
            case 1 :
                odRedeem.setTextColor(getResources().getColor(R.color.white));
                odRedeem.setBackgroundResource(R.drawable.redeem_border_2);

                marksRedeem.setTextColor(getResources().getColor(R.color.black));
                marksRedeem.setBackgroundResource(R.drawable.redeem_border_3);

                attendanceRedeem.setTextColor(getResources().getColor(R.color.black));
                attendanceRedeem.setBackgroundResource(R.drawable.redeem_border_3);

                break;

            case 2 :
                marksRedeem.setTextColor(getResources().getColor(R.color.white));
                marksRedeem.setBackgroundResource(R.drawable.redeem_border_2);

                odRedeem.setTextColor(getResources().getColor(R.color.black));
                odRedeem.setBackgroundResource(R.drawable.redeem_border_3);

                attendanceRedeem.setTextColor(getResources().getColor(R.color.black));
                attendanceRedeem.setBackgroundResource(R.drawable.redeem_border_3);

                break;

            case 3 :
                attendanceRedeem.setTextColor(getResources().getColor(R.color.white));
                attendanceRedeem.setBackgroundResource(R.drawable.redeem_border_2);

                odRedeem.setTextColor(getResources().getColor(R.color.black));
                odRedeem.setBackgroundResource(R.drawable.redeem_border_3);

                marksRedeem.setTextColor(getResources().getColor(R.color.black));
                marksRedeem.setBackgroundResource(R.drawable.redeem_border_3);

                break;
        }
    }

    private void redeemCoins() {

        int uid = sharedPreferences.getInt("userId",0);

        Map<String, String> params = new HashMap<>();
        params.put("userID", String.valueOf(uid));
        params.put("type",String.valueOf(type));

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                REDEEM, new JSONObject(params),
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

        jsonObjReq.setTag("RedeemRequest");
        requestQueue.add(jsonObjReq);
    }

    private void parseResponse(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Success!!", Snackbar.LENGTH_LONG);
                snackbar.show();

                if (type == 1) {
                    coins = coins - 100;
                } else if (type == 2) {
                    coins = coins - 200;
                } else {
                    coins = coins - 300;
                }
                setCoins();
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void setCoins() {
        sharedPreferences.edit().putInt("coins",coins).apply();
        tvCoins.setText(String.valueOf(coins));

        setRedeemData();
    }

    private void showRedeemDialog() {

        new AlertDialog.Builder(RedeemActivity.this)
                .setTitle("Redeem")
                .setMessage("Are you sure you want to redeem?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        redeemCoins();
                    }
                })
                .setNegativeButton("No",null)
                .show();
    }

    public class RedeemAdapter extends RecyclerView.Adapter<RedeemAdapter.ViewHolder> {

        private final ArrayList<Integer> redeemType_adapter;
        private final ArrayList<String> redeemDate_adapter;

        private RedeemAdapter(ArrayList<Integer> redeemType, ArrayList<String> redeemDate) {
            this.redeemType_adapter = redeemType;
            this.redeemDate_adapter = redeemDate;
        }

        @NonNull
        @Override
        public RedeemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_redeem, parent, false);
            return new RedeemAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RedeemAdapter.ViewHolder holder, int position) {
            if (redeemType_adapter.get(position) == 1) {
                //OD Redeem
                String title = "OD Request";
                String redeemAmt = "100 Points";

                holder.redeemTitle.setText(title);
//                holder.redeemAmount.setText("1");
                holder.redeemDesc.setText(redeemAmt);
            } else if (redeemType_adapter.get(position) == 2) {
                //Marks Redeem
                String title = "Marks Request";
                String redeemAmt = "200 Points";
//                holder.redeemAmount.setText("5");
                holder.redeemTitle.setText(title);
                holder.redeemDesc.setText(redeemAmt);
            } else {
                //Attendance Redeem
                String title = "Attendance Request";
                String redeemAmt = "300 Points";
//                holder.redeemAmount.setText("3");
                holder.redeemTitle.setText(title);
                holder.redeemDesc.setText(redeemAmt);
            }
            holder.redeemDate.setText(redeemDate_adapter.get(position));
        }

        @Override
        public int getItemCount() {
            return redeemType_adapter.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final TextView redeemTitle;
            final TextView redeemDesc;
            final TextView redeemAmount;
            final TextView redeemDate;

            ViewHolder(View itemView) {
                super(itemView);
                redeemTitle = itemView.findViewById(R.id.redeemTitle);
                redeemDesc = itemView.findViewById(R.id.redeemDesc);
                redeemAmount = itemView.findViewById(R.id.redeemAmount);
                redeemDate = itemView.findViewById(R.id.redeemDate);
            }
        }
    }

    private void setRedeemData() {
        requestQueue.add(requestRedeemData());
    }

    private JsonObjectRequest requestRedeemData() {

        redeemType.clear();
        redeemDate.clear();

        int uid = sharedPreferences.getInt("userId",0);
        String url = REDEEM_DETAILS + uid ;

        return new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        parseRedeemData(response);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }


    private void parseRedeemData(JSONObject jsonObject) {

        try {
            if (jsonObject.getBoolean("isSuccess")) {

                JSONArray jsonArray = jsonObject.getJSONArray("redeems");
                if (jsonArray.length() <= 0) {
                    rvRedeem.setVisibility(View.GONE);
                    tvRedeem.setVisibility(View.VISIBLE);
                } else {
                    rvRedeem.setVisibility(View.VISIBLE);
                    tvRedeem.setVisibility(View.GONE);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        redeemType.add(json.getInt("type"));

                        redeemDate.add(UTCToIST(json.getString("createdOn")));
                    }

                    rvRedeem.setHasFixedSize(true);
                    RedeemAdapter redeemAdapter = new RedeemAdapter(redeemType, redeemDate);
                    rvRedeem.setLayoutManager(new StaggeredGridLayoutManager(1,1));
                    rvRedeem.setAdapter(redeemAdapter);
                }
            }
            else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
