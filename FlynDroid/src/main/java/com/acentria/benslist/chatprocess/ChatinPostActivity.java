package com.acentria.benslist.chatprocess;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acentria.benslist.Account;
import com.acentria.benslist.R;
import com.acentria.benslist.Utils;
import com.acentria.benslist.response.ChatMessageResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatinPostActivity extends AppCompatActivity implements View.OnClickListener {
    private String user_id, merchant_id, post_id = "", usernameformarchent, account_name;
    private RecyclerView rv_recyclerviw;
    private LinearLayout ll_bottom;
    private TextView tv_no_records;
    private ImageView iv_send;
    private EditText et_send_massage;
    private ProgressDialog progressDialog;
    private String TAG = ChatinPostActivity.class.getName();
    private List<ChatMessageResponse> list = new ArrayList<>();


    private boolean is_chat_byer = false;
    private ChatOnPostAdatper adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Chat History");
        setContentView(R.layout.activity_chat_history_layout);
        ActionBar actionBar = getSupportActionBar();
        ((ActionBar) actionBar).setDisplayHomeAsUpEnabled(true);
        setweigits();
    }

    private void setweigits() {
        /*implent for chat*/

        account_name = Account.accountData.get("type_name");
        user_id = Account.accountData.get("id");
        if (getIntent().getExtras() != null) {
            post_id = getIntent().getStringExtra("post_id");
            merchant_id = getIntent().getStringExtra("seller_ID");
        }

        et_send_massage = findViewById(R.id.et_send_massage);
        iv_send = findViewById(R.id.iv_send);
        iv_send.setOnClickListener(this);
        rv_recyclerviw = findViewById(R.id.rv_recyclerviw);
        tv_no_records = findViewById(R.id.tv_no_records);
        ll_bottom = findViewById(R.id.ll_bottom);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading...");

        if (Utils.isOnline(this)) {
            checkuserblockORnot_callApi();
//            call_chat_historyApi();
        } else {
            rv_recyclerviw.setVisibility(View.GONE);
            tv_no_records.setVisibility(View.VISIBLE);
            tv_no_records.setText(getResources().getString(R.string.network_connection_error));
        }

    }

    private void checkuserblockORnot_callApi() {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("loginid", user_id)
                .addFormDataPart("otherid", merchant_id)
                .build();
        Request request = new Request.Builder()
                .url("https://www.benslist.com/chatMessageBlockUserApi.inc.php")
                .post(requestBody)
                .build();
        progressDialog.show();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Log.e(TAG, "error");
                        rv_recyclerviw.setVisibility(View.GONE);
                        tv_no_records.setVisibility(View.VISIBLE);
                        tv_no_records.setText(getResources().getString(R.string.server_error));
                        ll_bottom.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String mResponse = response.body().string();
                    Log.e(TAG, "user block onResponse: " + mResponse);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if (mResponse.equalsIgnoreCase("0")) {
                                if (Utils.isOnline(ChatinPostActivity.this)) {
                                    call_chat_historyApi();
                                } else {
                                    Toast.makeText(ChatinPostActivity.this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                rv_recyclerviw.setVisibility(View.GONE);
                                tv_no_records.setVisibility(View.VISIBLE);
                                tv_no_records.setText("This user has been blocked you");
                                Log.e(TAG, "user bolock set" + mResponse);
                                ll_bottom.setVisibility(View.GONE);
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void call_chat_historyApi() {

        if (user_id == null) {
            return;
        }

        if (post_id == null) {
            return;
        }

        if (!account_name.equalsIgnoreCase("Seller")) {
//            SEND_MESSAGE = "user_message";
            is_chat_byer = true;
        } else {
//            SEND_MESSAGE = "merchant_message";
            is_chat_byer = false;
        }


        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", user_id)
                .addFormDataPart("post_id", post_id)
                .build();
        Request request = new Request.Builder()
                .url("https://www.benslist.com/Api/Chat/chat_user_message_load.inc.php")
                .post(requestBody)
                .build();
        progressDialog.show();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Log.e(TAG, "error");
                        rv_recyclerviw.setVisibility(View.GONE);
                        tv_no_records.setVisibility(View.VISIBLE);
                        tv_no_records.setText(getResources().getString(R.string.server_error));
//                        Toast.makeText(ChatinPostActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        final String respo = response.body().string();
                        Log.e(TAG, "onResponse: \n" + respo);
                        if (!respo.equalsIgnoreCase("[]")) {
                            try {
                                JSONArray itemArray = new JSONArray(respo);
                                Type type = new TypeToken<ArrayList<ChatMessageResponse>>() {
                                }.getType();
                                list = (new Gson()).fromJson(itemArray.toString(), type);

//                                for (int i = 0; i < list.size(); i++) {
//                                    list.get(i).setMerchentName(usernameformarchent);
//                                    Log.e(TAG, "onResponse: set merchent name for chat history" + list.get(i).getMerchent_name());
//                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        tv_no_records.setVisibility(View.GONE);
                                        ll_bottom.setVisibility(View.VISIBLE);

                                        adapter = new ChatOnPostAdatper(ChatinPostActivity.this, list, is_chat_byer);
                                        rv_recyclerviw.setLayoutManager(new LinearLayoutManager(ChatinPostActivity.this));
                                        rv_recyclerviw.setAdapter(adapter);
                                        rv_recyclerviw.scrollToPosition(list.size() - 1);
//                                        adapter.notifyDataSetChanged();
//                                        rv_recyclerviw.setAdapter(new ChatMessageAdatper(ChatHistoryActivity.this, list, is_chat_byer));

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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                rv_recyclerviw.setVisibility(View.GONE);
                                tv_no_records.setVisibility(View.VISIBLE);
                                tv_no_records.setText("No record found");
                                Log.e(TAG, "unsuccess");

                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send:
                if (et_send_massage.getText().length() > 0) {
                    Log.e(TAG, "onClick: no message type than can't be send button");
                    if (Utils.isOnline(this)) {
                        call_sendmessage_Api(et_send_massage.getText().toString());
                        Utils.hideKeyboard(et_send_massage);
                    } else {
                        Toast.makeText(ChatinPostActivity.this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "onClick: no message type than can't be send button" + et_send_massage.getText().toString());
                }
                break;
        }
    }

    private void call_sendmessage_Api(String send_message) {
        String SEND_MESSAGE;
        if (send_message.isEmpty()) {
            return;
        } else {
            if (!account_name.equalsIgnoreCase("Seller")) {
                SEND_MESSAGE = "user_message";
                is_chat_byer = true;

            } else {
                SEND_MESSAGE = "merchant_message";
                is_chat_byer = false;
            }
        }


        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", user_id)
                .addFormDataPart("post_id", post_id)
                .addFormDataPart(SEND_MESSAGE, send_message)
                .build();

        Request request = new Request.Builder()
                .url("https://www.benslist.com/Api/Chat/chat_user_message.inc.php")
                .post(requestBody)
                .build();
        progressDialog.show();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(ChatinPostActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        final String respo = response.body().string();
                        Log.e(TAG, "onResponse: \n" + respo);
                        if (!respo.equalsIgnoreCase("[]")) {
                            try {
                                JSONArray itemArray = new JSONArray(respo);
                                Type type = new TypeToken<ArrayList<ChatMessageResponse>>() {
                                }.getType();
//                                ChatMessageResponse chatpojo=new ChatMessageResponse();

                                final List<ChatMessageResponse> newlist = (new Gson()).fromJson(itemArray.toString(), type);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        tv_no_records.setVisibility(View.GONE);
                                        et_send_massage.clearFocus();
                                        et_send_massage.setText("");
                                        Log.e(TAG, "run: " + list.size());
//                                          [{"user_id":"8","merchant_id":"1","user_message":"hmmmmm","merchant_message":"","date":"2019-08-09 12:05:18"}]
                                        ChatMessageResponse chatpojo = new ChatMessageResponse();
//                                        chatpojo.setMerchentName(usernameformarchent);
                                        chatpojo.setDate(newlist.get(0).getDate());
                                        chatpojo.setMerchantId(newlist.get(0).getMerchantId());
                                        chatpojo.setUserId(newlist.get(0).getUserId());
                                        chatpojo.setUserMessage(newlist.get(0).getUserMessage());
                                        chatpojo.setMerchantMessage(newlist.get(0).getMerchantMessage());
                                        list.add(chatpojo);

                                        Log.e(TAG, "run: list size after set data in pojo " + list.size());
                                        if (list.size() > 0) {

                                            if (list.size() == 1) {
                                                rv_recyclerviw.setVisibility(View.VISIBLE);
                                                adapter = new ChatOnPostAdatper(ChatinPostActivity.this, list, is_chat_byer);
                                                rv_recyclerviw.setLayoutManager(new LinearLayoutManager(ChatinPostActivity.this));
                                                rv_recyclerviw.setAdapter(adapter);
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                rv_recyclerviw.setVisibility(View.VISIBLE);
                                                adapter = new ChatOnPostAdatper(ChatinPostActivity.this, list, is_chat_byer); /**/
                                                adapter.notifyItemInserted(list.size() - 1);
                                                rv_recyclerviw.scrollToPosition(adapter.getItemCount() - 1);
                                                Log.e(TAG, "run: list size >0 " + list.size());
                                            }

                                        }
//                                        else {
//                                            adapter = new ChatOnPostAdatper(ChatinPostActivity.this, list, is_chat_byer);
//                                            rv_recyclerviw.setLayoutManager(new LinearLayoutManager(ChatinPostActivity.this));
//                                            rv_recyclerviw.setAdapter(adapter);
//                                            adapter.notifyDataSetChanged();
//
//                                            Log.e(TAG, "run: list size ==0 " + list.size());
//                                        }


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
//                                    rv_recyclerviw.setVisibility(View.GONE);
//                                    tv_no_records.setVisibility(View.VISIBLE);
//                                    tv_no_records.setText("No record found");
                                    Log.e(TAG, "arrary blank get" + respo);

                                }
                            });

                        }


                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
//                                rv_recyclerviw.setVisibility(View.GONE);
//                                tv_no_records.setVisibility(View.VISIBLE);
//                                tv_no_records.setText("No record found");
                                Log.e(TAG, "unsuccess" + "notsend message ");

                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
