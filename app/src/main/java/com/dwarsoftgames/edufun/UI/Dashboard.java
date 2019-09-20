package com.dwarsoftgames.edufun.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dwarsoftgames.edufun.CircleCountDownView;
import com.dwarsoftgames.edufun.Models.MenuModel;
import com.dwarsoftgames.edufun.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.dwarsoftgames.edufun.UI.MainActivity.SHAREDPREF;

public class Dashboard extends AppCompatActivity {

    private String PROFILE_DETAILS = "https://edufun.dwarsoft.com/api/edufun/Profile?UserID=";

    private DrawerLayout drawer;
    private List<MenuModel> listDataHeader;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;

    private TextView tvName;
    private TextView tvEmail;
    private CircleImageView imageView;
    private SharedPreferences sharedPreferences;
    private CircleCountDownView countDownView;

    private TextView tvCoins;
    private MaterialButton btRedeem;
    private TextView tvOD, tvMarks, tvAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setViews();
        initData();
        setSupportActionBar(toolbar);
        setDrawer();
        setOnClicks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RequestQueue requestQueue = Volley.newRequestQueue(Dashboard.this);
        requestQueue.add(setData());
    }

    private void setViews() {
        toolbar = findViewById(R.id.toolbar);
        imageView = findViewById(R.id.imageView);
        countDownView = findViewById(R.id.countDownView);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvCoins = findViewById(R.id.tvCoins);
        btRedeem = findViewById(R.id.btRedeem);
        tvOD = findViewById(R.id.tvOD);
        tvMarks = findViewById(R.id.tvMarks);
        tvAttendance = findViewById(R.id.tvAttendance);
    }

    private void setOnClicks() {
        btRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this,RedeemActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        Picasso.get().load(sharedPreferences.getString("profilePic","")).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(imageView);
        countDownView.setProgress(7500,10000);
    }

    private void setDrawer() {
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        RecyclerView list_drawer = findViewById(R.id.list_drawer);
        prepareListData();
        list_drawer.setLayoutManager(new LinearLayoutManager(this));
        DrawerAdapter drawerAdapter = new DrawerAdapter();
        list_drawer.setAdapter(drawerAdapter);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // show back button
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
                } else {
                    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    toggle.syncState();
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            drawer.openDrawer(GravityCompat.START);
                        }
                    });
                }
            }
        });
    }

    private void prepareListData() {

        listDataHeader = new ArrayList<>();

        MenuModel model1 = new MenuModel();
        model1.setTitle("Quiz");
        model1.setImage(R.drawable.ic_launcher_foreground);
        listDataHeader.add(model1);

        MenuModel model2 = new MenuModel();
        model2.setTitle("Assignments");
        model2.setImage(R.drawable.ic_launcher_foreground);
        listDataHeader.add(model2);

        MenuModel model3 = new MenuModel();
        model3.setTitle("Events");
        model3.setImage(R.drawable.ic_launcher_foreground);
        listDataHeader.add(model3);

        MenuModel model4 = new MenuModel();
        model4.setTitle("Doubts");
        model4.setImage(R.drawable.ic_launcher_foreground);
        listDataHeader.add(model4);
    }

    class DrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {
                view = LayoutInflater.from(Dashboard.this).inflate(R.layout.header_side_nav, parent, false);
                return new HeaderViewHolder(view);
            } else {
                view = LayoutInflater.from(Dashboard.this).inflate(R.layout.list_group, parent, false);
                return new ItemViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HeaderViewHolder) {
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            } else {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                MenuModel userModel = listDataHeader.get(position - 1);
                itemViewHolder.lblListHeader.setText(userModel.getTitle());
                itemViewHolder.img_simbol.setImageResource(userModel.getImage());
                itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchActivity(holder.getAdapterPosition());
                    }
                });
            }
        }

        private void switchActivity(int position) {
            switch (position) {
                case 1 :
                    Intent intent=new Intent(Dashboard.this,QuizActivity.class);
                    startActivity(intent);
                break;

                case 2 :
                    Intent intent1=new Intent(Dashboard.this,AssignmentActivity.class);
                    startActivity(intent1);
                    break;

                case 3 :
                    Intent intent2=new Intent(Dashboard.this,EventsActivity.class);
                    startActivity(intent2);
                    break;

                case 4 :
                    Intent intent3=new Intent(Dashboard.this,DoubtsActivity.class);
                    startActivity(intent3);
                    break;



            }
        }

        @Override
        public int getItemCount() {
            return listDataHeader.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return 0;
            else
                return 1;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            final TextView lblListHeader;
            final ImageView img_simbol;
            final LinearLayout lin_row;

            ItemViewHolder(View itemView) {
                super(itemView);
                lblListHeader = itemView.findViewById(R.id.lblListHeader);
                img_simbol = itemView.findViewById(R.id.img_simbol);
                lin_row = itemView.findViewById(R.id.lin_row);
            }
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder {

            private final MaterialButton btSign;
            private final TextView tvUser;

            HeaderViewHolder(View itemView) {
                super(itemView);

                btSign = itemView.findViewById(R.id.btSign);
                tvUser = itemView.findViewById(R.id.tvUser);
            }
        }
    }

    private JsonObjectRequest setData() {

        int uid = sharedPreferences.getInt("userId",0);
        String url = PROFILE_DETAILS + uid ;

        return new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        parseData(response);
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void parseData(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("isSuccess")) {
                String emailID = jsonObject.getString("emailID");
                String userName = jsonObject.getString("userName");
                String rollNo = jsonObject.getString("rollNo");
                int coins = jsonObject.getInt("coins");
                int OD = jsonObject.getInt("od");
                int Marks = jsonObject.getInt("marks");
                int Attendance = jsonObject.getInt("attendance");
                String att = Attendance + "%";

                sharedPreferences.edit().putInt("coins",coins).apply();

                tvName.setText(userName);
                tvEmail.setText(emailID);
                tvCoins.setText(String.valueOf(coins));
                tvOD.setText(String.valueOf(OD));
                tvMarks.setText(String.valueOf(Marks));
                tvAttendance.setText(att);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
