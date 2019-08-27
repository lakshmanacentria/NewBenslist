package com.acentria.benslist.controllers;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acentria.benslist.Config;
import com.acentria.benslist.CreateAccountActivity;
import com.acentria.benslist.Dialog;
import com.acentria.benslist.Lang;
import com.acentria.benslist.R;
import com.acentria.benslist.Utils;
import com.acentria.benslist.XMLParser;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;


import org.json.JSONException;
import org.json.JSONObject;


import java.util.Arrays;
import java.util.HashMap;


public class SocialRegistraionActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public static final int FB_SIGN_IN = 64206;
    private final String TAG = "SocialRegistraionActivity=> ";
    private CallbackManager callbackManager = CallbackManager.Factory.create();
    LoginButton fbLogin;
    SignInButton btn_gplush;
    private int resultcode = -1, request_code = 0;
    private int fbcode = 112;
    private String firstname, lastname, name, mobileNumber, social_id, link, emailaddress, password, social_type, passwordfb, countrycode = "", plush = "+", country;
    private GoogleApiClient mGoogleApiClient;
    public final static int RC_SIGN_IN = 007;
    private TextView tv_username, tv_email;
    private ImageView iv_profile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setTitle("Social Login");
        setContentView(R.layout.activity_social_registration_layout);
        ActionBar actionBar = getSupportActionBar();
        ((ActionBar) actionBar).setDisplayHomeAsUpEnabled(true);

        initwigits();
    }

    private void initwigits() {
        fbLogin = (LoginButton) findViewById(R.id.fbLogin);
        fbLogin.setReadPermissions(Arrays.asList("public_profile", "email"));
        callbackManager = CallbackManager.Factory.create();
        fbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {

                            @SuppressLint("LongLogTag")
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                // Application code
									/*Boolean ver = Boolean.parseBoolean(object.optString("verified"));
									if ( !ver ) {
										Dialog.simpleWarning(Lang.get("facebook_not_verified"), context);
									}
									else */
                                if (object != null) {
                                    Log.e(TAG, "onCompleted: JSONObject " + object);
                                    Log.e(TAG, "onCompleted: GraphResponse" + response);

                                    JSONObject json = response.getJSONObject();
                                    try {
                                        if (json != null) {
                                            if (json.getJSONObject("picture").getJSONObject("data") != null)
                                                link = json.getJSONObject("picture").getJSONObject("data").getString("url");
                                            else
                                                link = "";
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    final HashMap<String, String> formData = new HashMap<String, String>();
                                    formData.put("username", object.optString("name"));
                                    formData.put("password", "will-be-generated");
                                    formData.put("email", object.optString("email"));
                                    formData.put("account_type", "will-be-set");
                                    formData.put("fb_id", object.optString("id"));
                                    formData.put("first_name", object.optString("first_name"));
                                    formData.put("last_name", object.optString("last_name"));

                                    Log.e(TAG, "onCompleted: fb name " + object.optString("name"));
                                    Log.e(TAG, "onCompleted: imgurl" + link);
                                    Log.e(TAG, "onCompleted: fb payload for api formData" + formData);
                                    tv_username.setText(object.optString("name"));
                                    tv_email.setText(object.optString("email"));
                                    Glide.with(SocialRegistraionActivity.this).load(link).placeholder(R.mipmap.seller_no_photo).diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate().into(iv_profile);
                                    social_type = "FACEBOOK";
                                    serverRequestForCheckUser(object.optString("id"), social_type, object.optString("email"), object.optString("first_name").toLowerCase() + object.optString("last_name").toLowerCase(), link);


                                } else {
                                    Log.d(TAG, "FB connect no user");
                                }


                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,first_name,last_name,verified,birthday,picture.width(150).height(150)");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });


        /*google*/

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(SocialRegistraionActivity.this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btn_gplush = findViewById(R.id.btn_gplush);
        btn_gplush.setOnClickListener(this);
        tv_username = findViewById(R.id.tv_username);
        tv_email = findViewById(R.id.tv_email);
        iv_profile = findViewById(R.id.iv_profile);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FB_SIGN_IN:
                callbackManager.onActivityResult(requestCode, resultCode, data);
                break;
            case RC_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleGoogleSignInResult(result);
                Log.e("signinresult :", result + "" + "\nresultcode =>" + resultCode + "");
                if (mGoogleApiClient != null)
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                break;

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gplush:
                signOut();
                gplushLogin();
                break;
        }
    }


    @SuppressLint("LongLogTag")
    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.e(TAG, "DisplayName" + acct.getDisplayName());
            //btnSignIn.setVisibility(View.GONE);

            Log.e(TAG + "SuccessConnection", mGoogleApiClient.isConnected() + ""); //First Time log in false default
            emailaddress = acct.getEmail();
            link = "";
            social_type = "GMAIL";
            social_id = acct.getId();
//            CommonMethod.setPrefrence(UserLoginActivity.this, "UserLogin", "true");

            if (acct.getPhotoUrl() != null) {
                link = acct.getPhotoUrl().toString();
                Glide.with(this).load(link).placeholder(R.mipmap.seller_no_photo).diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate().into(iv_profile);

            }

            String fullname = acct.getDisplayName();
            String[] parts = fullname.split("\\s+");
            if (parts.length == 2) {
                firstname = parts[0];
                lastname = parts[1];
                Log.d("First-->", "" + firstname);
                Log.d("Last-->", "" + lastname);
            } else if (parts.length == 3) {
                firstname = parts[0];
                String middlename = parts[1];
                lastname = parts[2];
            }
            tv_username.setText(firstname + lastname);
            tv_email.setText(emailaddress);
            Log.e(TAG, "handleGoogleSignInResult: " + social_id + social_type + emailaddress + link);


            serverRequestForCheckUser(social_id, social_type, emailaddress, firstname.toLowerCase() + lastname.toLowerCase(), link);


            //updateUI(true);
        } else {
            // Log.e("econnection else :",mGoogleApiClient.isConnected()+"");

            // Signed out, show unauthenticated UI.
            // updateUI(false);
            Log.d("Logger", "handleSignInResult : Fail");
        }
    }

    @SuppressLint("LongLogTag")
    private void serverRequestForCheckUser(String social_id, String social_type, String emailaddress, String username, String profile_url) {


        /*if check user already register or not if user already register to send login page other wise its relocated to registration page for regeistration*/
        Log.e(TAG, "serverRequestForCheckUser: " + social_id + "\n" + social_type + "\n" + emailaddress + "\n" + username);
        Intent intent = new Intent(SocialRegistraionActivity.this, CreateAccountActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("email", emailaddress);
        intent.putExtra("profile_url",profile_url);
        startActivity(intent);
        finish();


    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                    }
                });
    }

    private void gplushLogin() {
        request_code = 0;
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.e("signin id", RC_SIGN_IN + "");

    }

    @SuppressLint("LongLogTag")
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, connectionResult + "", Toast.LENGTH_LONG).show();
    }
}
