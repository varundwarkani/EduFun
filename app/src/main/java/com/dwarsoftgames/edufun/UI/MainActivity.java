package com.dwarsoftgames.edufun.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dwarsoftgames.edufun.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener  {

    private String AUTH_USERS = "https://edufun.dwarsoft.com/api/edufun/Authentication/User";

    public static final String SHAREDPREF = "SHAREDPREF";
    private SharedPreferences sharedPreferences;
    private boolean loggedIn;
    private MaterialButton btLogin;

    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 30;

    private String name;
    private String email;
    private String profile_pic;
    private String rollNo;
    private int departmentCode;

    private ViewGroup viewGroup = null;
    private AlertDialog alertDialog;

    private RequestQueue requestQueue;
    private ArrayAdapter<String> spinnerAdapter_department;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setViews();
        initData();
        setOnClicks();
    }

    private void setViews() {
        btLogin = findViewById(R.id.btLogin);
    }

    private void initData() {
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        loggedIn = sharedPreferences.getBoolean("loggedIn",false);

        if (loggedIn) {
            openDashboard();
        } else {
            requestQueue = Volley.newRequestQueue(getApplicationContext());

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        }
    }

    private void setOnClicks() {
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleLogin();
            }
        });
    }

    private void googleLogin() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Google Sign In")
                .setMessage("Make sure to use college provided mail id")
                .setCancelable(false)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startGoogleLogin();
                    }
                })
                .setNegativeButton("No",null)
                .show();
    }

    private void startGoogleLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        if (completedTask.isComplete()) {
            if (completedTask.isSuccessful()) {
                try {
                    GoogleSignInAccount account = completedTask.getResult(ApiException.class);
                    if (account == null) {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Login failed. No account found!", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        return;
                    }
                    email = account.getEmail();

                    if (account.getPhotoUrl() == null) {
                        profile_pic = "none";
                    } else {
                        profile_pic = String.valueOf(account.getPhotoUrl());
                    }

                    assert email != null;
                    if (!email.contains(".edu.in")) {
                        mGoogleSignInClient.signOut();
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Google Sign In")
                                .setMessage("Make sure to use college provided mail id")
                                .setCancelable(false)
                                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startGoogleLogin();
                                    }
                                })
                                .setNegativeButton("No",null)
                                .show();
                    } else {
                        if (account.getDisplayName() == null) {
                            name = "error";
                        } else {
                            name = account.getDisplayName();
                        }

                        sharedPreferences.edit().putString("userName", name).apply();
                        sharedPreferences.edit().putString("emailId", email).apply();
                        sharedPreferences.edit().putString("profilePic",profile_pic).apply();

                        showRollDeptDialog();
                    }
                } catch (ApiException e) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Login failed", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            } else if (completedTask.isCanceled()) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Login cancelled.", Snackbar.LENGTH_SHORT);
                snackbar.show();
            } else {
                completedTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Login failed. Unknown error: "+e, Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                });
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Api Ex: "+connectionResult, Toast.LENGTH_LONG).show();
    }

    private void showRollDeptDialog() {
        AlertDialog.Builder builderr = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_login_dialog,viewGroup,false);
        builderr.setView(view);
        builderr.setCancelable(false);

        final EditText etRoll;
        MaterialButton btEnter;
        final MaterialSpinner spDepartment;

        etRoll = view.findViewById(R.id.etRoll);
        spDepartment = view.findViewById(R.id.spDepartment);
        btEnter = view.findViewById(R.id.btEnter);

        spDepartment.setHint("Select Department");
        spDepartment.setHintTextColor(getResources().getColor(R.color.black));

        spinnerAdapter_department = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter_department.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter_department.add("Select Department");
        spinnerAdapter_department.add("CSE");
        spinnerAdapter_department.add("ECE");
        spinnerAdapter_department.add("EEE");
        spinnerAdapter_department.add("CIVIL");
        spinnerAdapter_department.add("MECH");

        spDepartment.setAdapter(spinnerAdapter_department);

        btEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollNo = etRoll.getText().toString().trim();
                departmentCode = spDepartment.getSelectedIndex();
                if (departmentCode == 0) {
                    Toast.makeText(MainActivity.this, "Please select Department", Toast.LENGTH_SHORT).show();
                } else {
                    if (rollNo.length() <= 0) {
                        Toast.makeText(MainActivity.this, "Please enter proper Roll No", Toast.LENGTH_SHORT).show();
                    } else {
                        closeAlert();
                        authUser();
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

    private void authUser() {

        if (name == null) {
            name = "";
        }

        Map<String, String> params = new HashMap<>();
        params.put("username", name);
        params.put("emailid", email);
        params.put("rollNo", rollNo);
        params.put("deptID", String.valueOf(departmentCode));

        sharedPreferences.edit().putInt("DeptID",departmentCode).apply();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                AUTH_USERS, new JSONObject(params),
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
                sharedPreferences.edit().putInt("userId",jsonObject.getInt("userId")).apply();
                sharedPreferences.edit().putBoolean("loggedIn",true).apply();
                openDashboard();
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Server Error", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void openDashboard() {
        Intent intent = new Intent(MainActivity.this,Dashboard.class);
        startActivity(intent);
        finish();
    }
}
