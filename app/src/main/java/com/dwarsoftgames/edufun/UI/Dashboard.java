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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dwarsoftgames.edufun.Models.MenuModel;
import com.dwarsoftgames.edufun.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dashboard extends AppCompatActivity {

    private DrawerLayout drawer;
    private List<MenuModel> listDataHeader;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setViews();
        setSupportActionBar(toolbar);
        setDrawer();
    }

    private void setViews() {
        toolbar = findViewById(R.id.toolbar);
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
        model1.setTitle("Menu 1");
        model1.setImage(R.drawable.ic_launcher_foreground);
        listDataHeader.add(model1);

        MenuModel model2 = new MenuModel();
        model2.setTitle("Menu 2");
        model2.setImage(R.drawable.ic_launcher_foreground);
        listDataHeader.add(model2);
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
//                        switchActivity(holder.getAdapterPosition());
                    }
                });
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
}
