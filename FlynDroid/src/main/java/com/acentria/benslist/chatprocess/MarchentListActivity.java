package com.acentria.benslist.chatprocess;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.acentria.benslist.Account;
import com.acentria.benslist.Config;
import com.acentria.benslist.Lang;
import com.acentria.benslist.R;
import com.acentria.benslist.Utils;
import com.acentria.benslist.response.MarchentListResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.http.Multipart;

public class MarchentListActivity extends AppCompatActivity implements Marchent_ListAdapter.OnClickPosi {


    private RecyclerView rv_recyclerviw;
    private TextView tv_no_records;
    private ProgressDialog progressDialog;
    private String TAG = MarchentListActivity.class.getName(), accountname;
    private List<MarchentListResponse> list;
    private boolean is_chatpost = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Chat now");
        setContentView(R.layout.activity_chat_layout);
        ActionBar actionBar = getSupportActionBar();
        ((ActionBar) actionBar).setDisplayHomeAsUpEnabled(true);
        setweigits();

    }

    private void setweigits() {
        rv_recyclerviw = findViewById(R.id.rv_recyclerviw);
        tv_no_records = findViewById(R.id.tv_no_records);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading...");

        if (Utils.isOnline(MarchentListActivity.this)) {
            call_marchrentlistApi();
        } else {
            tv_no_records.setVisibility(View.VISIBLE);
            rv_recyclerviw.setVisibility(View.GONE);
            tv_no_records.setText(getResources().getString(R.string.network_connection_error));
//            Toast.makeText(this, getResources().getText(R.string.network_connection_error), Toast.LENGTH_LONG).show();
        }


    }

    private void call_marchrentlistApi() {
        final String account_id;
        String account_type, url;
        if (!Account.loggedIn) {

            return;
        } else {
            account_id = Account.accountData.get("id");
            accountname = Account.accountData.get("type_name");
            account_type = "sender_id";
            Log.e(TAG, "account_id=> " + account_id);
            Log.e(TAG, "accounttype: " + accountname);
            url = "https://www.benslist.com/Api/Chat/chat_users_list.inc.php";

//            if (accountname.equalsIgnoreCase("Seller")) {
//                acount_type = "merchant_id";
//                url = "https://www.benslist.com/Api/Chat/chat_user_list.inc.php";
////                is_chatpost = false;
//
//                Log.e(TAG, "type " + acount_type + "\taccount_id" + account_id + "\nurl " + url);
//            } else {
//                acount_type = "user_id";
//                url = "https://www.benslist.com/Api/Chat/chat_merchant_list.inc.php";
//                Log.e(TAG, "type " + acount_type + "account_id" + account_id + "\nurl " + url);
////                is_chatpost = true;
//                Log.e(TAG, "type " + acount_type + "\taccount_id" + account_id + "\nurl " + url);
//
//
//            }
        }
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(account_type, account_id)
                .build();


        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        progressDialog.show();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Log.e(TAG, "error");
                        rv_recyclerviw.setVisibility(View.GONE);
                        tv_no_records.setVisibility(View.VISIBLE);
                        tv_no_records.setText(getResources().getString(R.string.server_error));
                        Toast.makeText(MarchentListActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String respo = response.body().string();
                    Log.e(TAG, "onResponse: \n" + respo);
                    if (!respo.equalsIgnoreCase("[]")) {
                        try {
                            JSONArray itemArray = new JSONArray(respo);
                            Type type = new TypeToken<ArrayList<MarchentListResponse>>() {
                            }.getType();
                            list = (new Gson()).fromJson(itemArray.toString(), type);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    tv_no_records.setVisibility(View.GONE);
                                    rv_recyclerviw.setLayoutManager(new LinearLayoutManager(MarchentListActivity.this));
                                    rv_recyclerviw.setAdapter(new Marchent_ListAdapter(MarchentListActivity.this, list, MarchentListActivity.this, is_chatpost));
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                rv_recyclerviw.setVisibility(View.GONE);
                                tv_no_records.setVisibility(View.VISIBLE);
                                tv_no_records.setText("There is no visitor yet");
                                Log.e(TAG, "arrary blank get" + respo);
                            }
                        });


                    }


                } else {
                    progressDialog.dismiss();
                    rv_recyclerviw.setVisibility(View.GONE);
                    tv_no_records.setVisibility(View.VISIBLE);
                    tv_no_records.setText("No record found");
                    Log.e(TAG, "unsuccess");

                }
            }
        });


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
    public void OnPosiClieck(int pos, String user_login_id, String marchentid, String post_id, String username) {
        Log.e(TAG, "OnPosiClieck " + pos + "\tUserlogin id " + user_login_id + "\t marchent_id" + marchentid + "\tpost_id " + post_id + "\tusername" + username);
        if (!is_chatpost) {
            if (accountname.equalsIgnoreCase("Seller")) {
                /*byer side login*/

                if (post_id.equalsIgnoreCase("blockAdmin")) {
//                    Toast.makeText(this, "Block user", Toast.LENGTH_LONG).show();
                    if (Utils.isOnline(this)) {
                        callBlockuseruserApi(marchentid);

                    } else {
                        Toast.makeText(MarchentListActivity.this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
                    }
                    Log.e(TAG, "OnPosiClieck: post_id 1 " + post_id);

                } else {
                    startActivity(new Intent(this, ChatPostListActivity.class).putExtra("merchant_id", marchentid).putExtra("user_name", username)
                            .putExtra("account_name", accountname));
                    Log.e(TAG, "OnPosiClieck: post_id 2  " + post_id);

                }


            } else {
                /*saller side login*/
                Log.e(TAG, "OnPosiClieck: post_id 3 " + post_id);
                startActivity(new Intent(this, ChatPostListActivity.class).putExtra("merchant_id", marchentid).putExtra("user_name", username)
                        .putExtra("account_name", accountname));

            }

        }
        /*Only come if becone this actvity send ischatpost boolean false than we need only call false condition*/

    }

    private void callBlockuseruserApi(String other_id) {
        Log.e(TAG, "callBlockuseruserApi: other_id " + other_id + "\nloginid " + Account.accountData.get("id"));
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("loginid", Account.accountData.get("id"))
                .addFormDataPart("otherid", other_id)
                .build();

//        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//        JsonObject jsonpayload = new JsonObject();
//        jsonpayload.addProperty("loginid", Account.accountData.get("id"));
//        jsonpayload.addProperty("otherid", other_id);
//        Log.e(TAG, "callBlockuseruserApi: jsonpayload " + jsonpayload);
//        RequestBody body = RequestBody.create(JSON, String.valueOf(jsonpayload));

        Request request = new Request.Builder()
                .url("https://benslist.com/chatUserBlockApi.inc.php")
                .post(requestBody)
                .build();

//        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://benslist.com/chatMessageBlockUserApi.inc.php").newBuilder();
//        urlBuilder.addQueryParameter("loginid", Account.accountData.get("id"));
//        urlBuilder.addQueryParameter("otherid", other_id);
//        String url = urlBuilder.build().toString();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .build();


        progressDialog.show();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(MarchentListActivity.this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String rResponse = response.body().string();
                        Log.e(TAG, "onResponse: " + rResponse);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
//                                Toast.makeText(MarchentListActivity.this, "block admin", Toast.LENGTH_LONG).show();
//                                Config.alertDailog(MarchentListActivity.this, "User blocked");
                                Utils.isAlertBox(MarchentListActivity.this, "User blocked", "");
                            }
                        });
//

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(MarchentListActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        /*not use*/

    }
}
