package com.acentria.benslist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.acentria.benslist.adapters.SpinnerAdapter;
import com.acentria.benslist.controllers.AccountArea;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;


public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "CreateAccountActivity";
    public final static int RESULT_ACCEPT = 215;
    private static HashMap<String, String> formData = new HashMap<String, String>();
    private static CreateAccountActivity instance;
    private static Context context;
    private static Intent intent;
    private static HashMap<String, View> fieldViews = new HashMap<String, View>();
    public static HashMap<String, LinkedHashMap<String, HashMap<String, String>>> agreementsFields = new HashMap<String, LinkedHashMap<String, HashMap<String, String>>>();
    public static HashMap<String, LinkedHashMap<String, HashMap<String, String>>> aFields = new HashMap<String, LinkedHashMap<String, HashMap<String, String>>>();
    public static HashMap<String, HashMap<String, ArrayList<HashMap<String, String>>>> items_data = new HashMap<String, HashMap<String, ArrayList<HashMap<String, String>>>>();

    /*fb and gplush */
    private boolean is_selected = false;
    private CallbackManager callbackManager;
    private int resultcode = -1, request_code = 0;
    private int fbcode = 112;
    private String firstname, lastname, name, mobileNumber, social_id, link, emailaddress, password, social_type, passwordfb, countrycode = "", plush = "+", country;
    private GoogleApiClient mGoogleApiClient;
    public final static int RC_SIGN_IN = 007;
    private EditText username, email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        context = this;
        intent = getIntent();

        Lang.setDirection(this);

        setTitle(Lang.get("title_activity_create_account"));
        setContentView(R.layout.activity_create_account);

        final View parent = findViewById(R.id.ca_parent);

        /* enable back action */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setFacebook_sdk();

        /* prepare account type spinner */
        final Spinner spinner = (Spinner) findViewById(R.id.type);

        final ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> caption = new HashMap<String, String>();
        caption.put("name", Lang.get("account_type"));
        caption.put("key", "");
        items.add(caption);

        if (aFields.isEmpty()) {
            loadData();
        }

        for (Entry<String, HashMap<String, String>> entry : Config.cacheAccountTypes.entrySet()) {
            items.add(entry.getValue());
        }

        HashMap<String, String> field_info = new HashMap<String, String>();
        field_info.put("key", "account_type");
        field_info.put("data", "");
        SpinnerAdapter adapter = new SpinnerAdapter(this, items, field_info, formData, null);

        spinner.setAdapter(adapter);

        final LinearLayout agreements = (LinearLayout) parent.findViewById(R.id.agreements);
        final LinearLayout second_step = (LinearLayout) parent.findViewById(R.id.second_step);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int position, long arg3) {
                fieldViews.clear();
                formData.put("account_type", items.get(position).get("key"));
                if (Utils.getCacheConfig("android_second_step") != null && Utils.getCacheConfig("android_second_step").equals("1")) {
                    second_step.setVisibility(View.VISIBLE);
                    second_step.removeAllViews();
                    if (position > 0 && aFields.get(formData.get("account_type")) != null) {
                        Forms.buildSubmitFields(second_step, aFields.get(formData.get("account_type")), formData, items_data.get(formData.get("account_type")), context, EditProfileActivity.actionbarSpinner, fieldViews, false);

                    }
                }

                if (!agreementsFields.isEmpty()) {
                    agreements.setVisibility(View.VISIBLE);
                    agreements.removeAllViews();
                    if (position > 0 && agreementsFields.get(formData.get("account_type")) != null) {
                        buildAgreemntsFields(agreements, agreementsFields.get(formData.get("account_type")), formData);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        if (Utils.getCacheConfig("account_login_mode").equals("email")) {
            username.setVisibility(View.GONE);
        } else {
            username.setVisibility(View.VISIBLE);
        }

        /* submit button handler */
        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                /* validate form */
                Boolean error = false;

                // username
                Matcher matcher = Pattern.compile("([a-zA-Z0-9\\-]+)").matcher(username.getText().toString());
                if (!Utils.getCacheConfig("account_login_mode").equals("email")) {
                    if (username.getText().toString().isEmpty()) {
                        error = true;
                        username.requestFocus();
                        username.setError(Lang.get("no_field_value"));
                    } else if (username.getText().toString().length() < 3) {
                        error = true;
                        username.requestFocus();
                        username.setError(Lang.get("notice_reg_length").replace("{field}", Lang.get("android_hint_username")));
                    } else if (!matcher.matches()) {
                        error = true;
                        username.requestFocus();
                        username.setError(Lang.get("invalid_username"));
                    } else {
                        formData.remove("username");
                        formData.put("username", username.getText().toString());
                    }
                }

                // password
                EditText password = (EditText) findViewById(R.id.password);
                matcher = Pattern.compile("((?=.*\\d)(?=.*[a-z]).{5,20})").matcher(password.getText().toString());

                if (!matcher.matches()) {
                    if (!error) {
                        error = true;
                        password.requestFocus();
                        password.setError(Lang.get("password_weak"));
                    }
                } else {
                    formData.remove("password");
                    formData.put("password", password.getText().toString());
                }

                // email
                /*final EditText email = (EditText) findViewById(R.id.email);*/
                matcher = Pattern.compile("(.+@.+\\.[a-z]+)").matcher(email.getText().toString());

                if (!matcher.matches()) {
                    if (!error) {
                        error = true;
                        email.requestFocus();
                        email.setError(Lang.get("bad_email"));
                    }
                } else {
                    formData.remove("email");
                    formData.put("email", email.getText().toString());
                }

                // account type
                if (formData.get("account_type") != null && formData.get("account_type").isEmpty()) {
                    error = true;
                    Dialog.simpleWarning(Lang.get("account_type_empty"), instance);
                } else {
                    if (Utils.getCacheConfig("android_second_step") != null && Utils.getCacheConfig("android_second_step").equals("1") && aFields.get(formData.get("account_type")) != null) {

                        if (!Forms.validate(formData, aFields.get(formData.get("account_type")), fieldViews)) {
                            error = true;
                        }
                    }
                    if (agreementsFields != null && !agreementsFields.isEmpty() && !Forms.validate(formData, agreementsFields.get(formData.get("account_type")), fieldViews)) {
                        error = true;
                    }
                }

                if (!error) {
                    /* show progressbar */
                    final ProgressDialog progress = ProgressDialog.show(instance, null, Lang.get("loading"));

                    /* hide keyboard */
                    Utils.hideKeyboard(parent.findFocus(), instance);

                    /* do request */
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.setTimeout(30000); // set 30 seconds for this task

                    final String url = Utils.buildRequestUrl("createAccount");
                    client.post(url, Utils.toParams(formData), new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] server_response) {
                            // called when response HTTP status is "200 OK"
                            try {
                                String response = String.valueOf(new String(server_response, "UTF-8"));
                                /* hide progressbar */
                                progress.dismiss();

                                /* parse xml response */
                                XMLParser parser = new XMLParser();
                                Document doc = parser.getDomElement(response, url);

                                if (doc == null) {
                                    Dialog.simpleWarning(Lang.get("returned_xml_failed"), instance);
                                } else {
                                    NodeList errorsNode = doc.getElementsByTagName("errors");

                                    /* handle errors */
                                    if (errorsNode.getLength() > 0) {
                                        Element element = (Element) errorsNode.item(0);
                                        NodeList errors = element.getChildNodes();

                                        if (errors.getLength() > 0) {
                                            Element error = (Element) errors.item(0);

                                            EditText field = username;
                                            if (error.getAttribute("field").equals("email")) {
                                                field = email;
                                            }

                                            field.setError(Lang.get(error.getTextContent()));
                                            field.requestFocus();
                                        }
                                    }
                                    /* process login */
                                    else {
                                        NodeList accountNode = doc.getElementsByTagName("account");
                                        AccountArea.confirmLogin(accountNode);

                                        /* finish this activity and show toast */
                                        instance.finish();

                                        String phrase = Account.accountData.get("status").equals("incomplete") ? Lang.get("registration_incompleted") : Lang.get("registration_completed");
                                        Toast.makeText(Config.context, phrase, Toast.LENGTH_LONG).show();
                                    }
                                }

                            } catch (UnsupportedEncodingException e1) {

                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        }
                    });
                }
            }
        });


        /*fb and gplush btn  initilization*/
        LoginButton fbLogin = (LoginButton) findViewById(R.id.fbLogin);
        SignInButton btn_gplush = (SignInButton) findViewById(R.id.btn_gplush);
        btn_gplush.setOnClickListener(this);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(CreateAccountActivity.this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        boolean loggedOut = AccessToken.getCurrentAccessToken() == null;

        if (!loggedOut) {
            if (Profile.getCurrentProfile() != null) {
//                Glide.with(this).load(Profile.getCurrentProfile().getProfilePictureUri(200, 200)).into(imageView);
                Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName());
            }


            //Using Graph API
            getUserProfile(AccessToken.getCurrentAccessToken());
        }

        AccessTokenTracker fbTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                if (accessToken2 == null) {
                    username.setText(null);
                    email.setText(null);
//                    imageView.setImageResource(0);
                    Toast.makeText(CreateAccountActivity.this, "User Logged Out.", Toast.LENGTH_LONG).show();
                }
            }
        };
        fbTracker.startTracking();

        fbLogin.setReadPermissions(Arrays.asList("email", "public_profile"));
        callbackManager = CallbackManager.Factory.create();
        fbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                //loginResult.getAccessToken();
                //loginResult.getRecentlyDeniedPermissions()
                //loginResult.getRecentlyGrantedPermissions()
                boolean loggedOut = AccessToken.getCurrentAccessToken() == null;

                if (!loggedOut) {
                    if (Profile.getCurrentProfile() != null) {
//                        Glide.with(CreateAccountActivity.this).load(Profile.getCurrentProfile().getProfilePictureUri(200, 200)).into(imageView);
                        Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName());

                    }


                    //Using Graph API
                    getUserProfile(AccessToken.getCurrentAccessToken());
                }

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });



        /*end of oncreate*/
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.e("TAG", object.toString());
                        try {
                            String first_name = object.getString("first_name");
                            String last_name = object.getString("last_name");
                            String emailstr = object.getString("email");
                            String id = object.getString("id");
                            String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                            username.setText("First Name: " + first_name + "\nLast Name: " + last_name + "\n" + image_url);
                            email.setText(emailstr);
//                            Glide.with(CreateAccountActivity.this).load(image_url).into(imageView);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void setFacebook_sdk() {
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
    }



    /*...........fb gplush */

    private void loadData() {
        HashMap<String, String> params = new HashMap<String, String>();
        final String url = Utils.buildRequestUrl("getAccountForms", params, null);
        final ProgressDialog progress = ProgressDialog.show(instance, null, Lang.get("android_sync_with_server"));
        /* do async request */

        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] server_response) {
                // called when response HTTP status is "200 OK"
                try {
                    String response = String.valueOf(new String(server_response, "UTF-8"));
                    /* hide progressbar */
                    XMLParser parser = new XMLParser();
                    Document doc = parser.getDomElement(response, url);
                    progress.dismiss();
                    if (doc == null) {
                        Dialog.simpleWarning(Lang.get("returned_xml_failed"), Config.context);
                    } else {

                        NodeList listingNode = doc.getElementsByTagName("items");

                        Element nlE = (Element) listingNode.item(0);
                        NodeList listing = nlE.getChildNodes();

                        for (int i = 0; i < listing.getLength(); i++) {
                            Element node = (Element) listing.item(i);
                            if (node.getTagName().equals("fields")) {
                                parseForm(node.getChildNodes());
                            } else if (node.getTagName().equals("agreement")) {
                                parseAgreementFields(node.getChildNodes());
                            }
                        }
                    }
                } catch (UnsupportedEncodingException e1) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        });
    }

    public static void parseForm(NodeList fields) {
        for (int i = 0; i < fields.getLength(); i++) {
            Element tag = (Element) fields.item(i);
            NodeList nodes = tag.getChildNodes();
            if (nodes.getLength() > 0) {
                HashMap<String, ArrayList<HashMap<String, String>>> items = new HashMap<String, ArrayList<HashMap<String, String>>>();
                aFields.put(tag.getTagName(), Account.parseForm(nodes, items));
                items_data.put(tag.getTagName(), items);
            }
        }
    }

    public static void parseAgreementFields(NodeList fields) {
        for (int i = 0; i < fields.getLength(); i++) {
            Element tag = (Element) fields.item(i);
            NodeList nodes = tag.getChildNodes();
            if (nodes.getLength() > 0) {
                LinkedHashMap<String, HashMap<String, String>> out = new LinkedHashMap<String, HashMap<String, String>>();
                for (int j = 0; j < nodes.getLength(); j++) {
                    HashMap<String, String> tmpField = new HashMap<String, String>();

                    Element field = (Element) nodes.item(j);
                    tmpField.put("key", Utils.getNodeByName(field, "key"));
                    tmpField.put("type", Utils.getNodeByName(field, "type"));
                    tmpField.put("page_type", Utils.getNodeByName(field, "page_type"));
                    tmpField.put("content", Utils.getNodeByName(field, "content"));
                    tmpField.put("name", Utils.getNodeByName(field, "name"));
                    tmpField.put("required", "1");
                    tmpField.put("mode", tmpField.get("page_type").equals("static") ? "view" : "webview");
                    out.put(Utils.getNodeByName(field, "key"), tmpField);

                }

                agreementsFields.put(tag.getTagName(), out);
            }
        }
    }

    private void buildAgreemntsFields(LinearLayout layout, LinkedHashMap<String, HashMap<String, String>> fields, final HashMap<String, String> formData) {

        for (Entry<String, HashMap<String, String>> entry : fields.entrySet()) {
            final HashMap<String, String> field = entry.getValue();
            final LinearLayout acceptField = (LinearLayout) Config.context.getLayoutInflater()
                    .inflate(R.layout.field_accept, null);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, Utils.dp2px(15), 0, 0);
            acceptField.setLayoutParams(params);

            final CheckBox checkbox = (CheckBox) acceptField.findViewById(R.id.accept_field);
            checkbox.setText(Lang.get("android_i_accept"));
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    /* save value */
                    formData.put(field.get("key"), isChecked ? "1" : "");
                    Forms.unsetError(acceptField);
                }
            });
            final TextView terms = (TextView) acceptField.findViewById(R.id.terms_field);
            terms.setText(field.get("name"));
            terms.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(Config.context, AcceptActivity.class);
                    intent.putExtra("key", field.get("key"));
                    intent.putExtra("name", field.get("name"));
                    intent.putExtra("mode", field.get("mode"));
                    intent.putExtra("data", field.get("content"));
                    ((Activity) instance).startActivityForResult(intent, RESULT_ACCEPT);
                }
            });

            layout.addView(acceptField);
            if (fieldViews != null) {
                fieldViews.put(field.get("key"), acceptField);
            }
        }
    }

    public static void confirmAccept(String key) {
        LinearLayout accept = (LinearLayout) fieldViews.get(key);
        CheckBox acceptField = (CheckBox) accept.findViewById(R.id.accept_field);
        acceptField.setChecked(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_ACCEPT:
                if (resultCode == RESULT_OK) {
                    confirmAccept(data.getStringExtra("key"));
                }

                break;
            case RC_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleGoogleSignInResult(result);
                Log.e("signinresult :", result + "" + "\nresultcode =>" + resultCode + "");
                if (mGoogleApiClient != null)
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                break;
            case 101:
                callbackManager.onActivityResult(requestCode, resultCode, data);
//                RequestFbData();
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
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
            username.setText(firstname + lastname);
            email.setText(emailaddress);
            Log.e(TAG, "handleGoogleSignInResult: " + social_id + social_type + emailaddress);

//            serverRequestForCheckUser(social_id, social_type, emailaddress);


            //updateUI(true);
        } else {
            // Log.e("econnection else :",mGoogleApiClient.isConnected()+"");

            // Signed out, show unauthenticated UI.
            // updateUI(false);
            Log.d("Logger", "handleSignInResult : Fail");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, connectionResult + "", Toast.LENGTH_LONG).show();
    }
}