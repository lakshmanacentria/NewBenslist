package com.acentria.benslist.chatprocess;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acentria.benslist.Account;
import com.acentria.benslist.ListingDetailsActivity;
import com.acentria.benslist.R;
import com.acentria.benslist.Utils;
import com.acentria.benslist.controllers.AccountArea;
import com.acentria.benslist.response.ChatPostResponse;
import com.acentria.benslist.response.MarchentListResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.http.Multipart;

public class ChatPostListActivity extends AppCompatActivity implements Marchent_ListAdapter.OnClickPosi {

    private RecyclerView rv_recyclerviw;
    private TextView tv_no_records;


    private ProgressDialog progressDialog;
    private String TAG = ChatPostListActivity.class.getName();
    private List<ChatPostResponse> list;
    private String marchent_id = "", username = "", account_name = "";
    private boolean is_chatpost = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Chat Post List");
        setContentView(R.layout.activity_chat_layout);
        ActionBar actionBar = getSupportActionBar();
        ((ActionBar) actionBar).setDisplayHomeAsUpEnabled(true);
        setweigits();

    }

    private void setweigits() {
        rv_recyclerviw = findViewById(R.id.rv_recyclerviw);
        tv_no_records = findViewById(R.id.tv_no_records);

        if (getIntent().getExtras() != null) {
            /*if condition not working because we have only one one Seller typer user  *laksh* */
            if (!getIntent().getStringExtra("account_name").equalsIgnoreCase("Seller")) {
                marchent_id = getIntent().getStringExtra("merchant_id");
                username = getIntent().getStringExtra("user_name");
                account_name = getIntent().getStringExtra("account_name");
                Log.e(TAG, "setweigits: " + "userside marchent id" + marchent_id + "username" + username + "account name " + account_name);
            } else {
                account_name = getIntent().getStringExtra("account_name");
                marchent_id = getIntent().getStringExtra("merchant_id");
                username = getIntent().getStringExtra("user_name");
                account_name = getIntent().getStringExtra("account_name");
                Log.e(TAG, "setweigits: " + "seller side user id" + marchent_id + "username" + username + "account name " + account_name);

            }

        }


        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading...");

        if (Utils.isOnline(this)) {
            call_chatpostApi();
        } else {
            tv_no_records.setVisibility(View.VISIBLE);
            rv_recyclerviw.setVisibility(View.GONE);
            tv_no_records.setText(getResources().getString(R.string.network_connection_error));
//            Toast.makeText(this, getResources().getText(R.string.network_connection_error), Toast.LENGTH_LONG).show();
        }


    }

    private void call_chatpostApi() {
//        final String acUser_id;
//        final String acMerchent_id;
//        String MERCHENT_ID = "merchant_id", USER_ID = "user_id";
        if (!Account.loggedIn) {
            return;
        }
        Log.e(TAG, "call_chatpostApi: Login userid" + Account.accountData.get("id") + "reciever_userid " + marchent_id);
//        else {
////            if (!account_name.equalsIgnoreCase("Seller")) {
////                MERCHENT_ID = "merchant_id";
////                USER_ID = "user_id";
////                acUser_id = Account.accountData.get("id");
////                acMerchent_id = marchent_id;
////                Log.e(TAG, "from Dealer side" + "account_id=> " + acUser_id + "\tMarchent id " + acMerchent_id);
////            } else {
//////                MERCHENT_ID = "merchant_id";
////                USER_ID = "user_id";
////                acUser_id = marchent_id;
////                acMerchent_id = Account.accountData.get("id");
////                Log.e(TAG, "from Seller side" + "account_id=> " + acUser_id + "\tMarchent id " + acMerchent_id);
////            }
//
//        }
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
//                .addFormDataPart("user_id", Account.accountData.get("id"))
//                .addFormDataPart("merchant_id", marchent_id)
                .addFormDataPart("reciever_userid", marchent_id)
                .addFormDataPart("userid", Account.accountData.get("id"))
                .build();

        Request request = new Request.Builder()
                .url("https://www.benslist.com/Api/Chat/chat_posts_list.inc.php")
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
                        Toast.makeText(ChatPostListActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
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
                            Type type = new TypeToken<ArrayList<ChatPostResponse>>() {
                            }.getType();
                            list = (new Gson()).fromJson(itemArray.toString(), type);

                            for (int i = 0; i < list.size(); i++) {
                                list.get(i).setUserName(username);
                                list.get(i).setReceiver_id(marchent_id);
                                list.get(i).setUserlogin_id(Account.accountData.get("id"));
                                Log.e(TAG, "onResponse: setpotion wise username posi " + i + "\t" + list.get(i).getUsername());
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    tv_no_records.setVisibility(View.GONE);
                                    rv_recyclerviw.setLayoutManager(new LinearLayoutManager(ChatPostListActivity.this));
                                    rv_recyclerviw.setAdapter(new Marchent_ListAdapter(ChatPostListActivity.this, ChatPostListActivity.this, list, is_chatpost));
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
                                tv_no_records.setText("No record found");


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
    public void OnPosiClieck(int pos, String user_login_id, String receiver_id, String post_id, String username) {

        Log.e(TAG, "OnPosiClieck " + pos + "\tUserlogin id " + user_login_id + "\t marchent_id" + receiver_id + "\tpostid" + post_id);
        if (is_chatpost) {
            /*  got to next screnen*/
            Log.e(TAG, "got to next ChatHistoryActivity");
            if (user_login_id.equalsIgnoreCase("delete_chat")) {
//                Toast.makeText(ChatPostListActivity.this, "delete chat", Toast.LENGTH_LONG).show();
                if (Utils.isOnline(ChatPostListActivity.this)) {
                    call_delete_chathistory(receiver_id, post_id);
                } else {
                    Toast.makeText(ChatPostListActivity.this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
                }
            } else if (user_login_id.equalsIgnoreCase("report_chat")) {
                if (Utils.isOnline(ChatPostListActivity.this)) {
                    startActivity(new Intent(ChatPostListActivity.this, ChatHistoryReportActivity.class)
                            .putExtra("marchentid", receiver_id)
                            .putExtra("post_id", post_id));
                } else {
                    Toast.makeText(ChatPostListActivity.this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
                }
            } else {
                startActivity(new Intent(this, ChatHistoryActivity.class).putExtra("user_id", user_login_id)
                        .putExtra("merchant_id", receiver_id).putExtra("post_id", post_id).putExtra("username", username)
                        .putExtra("account_name", account_name));
            }
        }
        /*Only come if becone this actvity send ischatpost boolean true than we need only call true condition*/
    }


    private void call_delete_chathistory(String marchent_id, String post_id) {
        OkHttpClient okHttpClient = new OkHttpClient();
//        RequestBody formBody = new FormBody.Builder()
//                .add("uid", Account.accountData.get("id"))
//                .add("mid", marchent_id)
//                .add("postid", post_id)
//                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uid", Account.accountData.get("id"))
                .addFormDataPart("mid", marchent_id)
                .addFormDataPart("postid", post_id)
                .build();
        final Request request = new Request.Builder().
                url("https://benslist.com/chatPostDeleteApi.inc.php")
                .post(requestBody).build();
//        Response response = okHttpClient.newCall(request).execute();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(ChatPostListActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    try {
                        String mResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(mResponse);
                        final String result = jsonObject.getString("success");
                        Log.e(TAG, "onResponse: " + mResponse);


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                if (result.equalsIgnoreCase("success")) {
                                    Utils.isAlertBox(ChatPostListActivity.this, "", "Chat history delete successfully");
                                }

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    progressDialog.dismiss();
                    Log.e(TAG, "onResponse:  " + getResources().getString(R.string.server_error));
                }

            }
        });


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

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


}
