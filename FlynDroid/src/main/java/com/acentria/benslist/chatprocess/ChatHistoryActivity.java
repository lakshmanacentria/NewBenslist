package com.acentria.benslist.chatprocess;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acentria.benslist.Account;
import com.acentria.benslist.R;
import com.acentria.benslist.Utils;
import com.acentria.benslist.response.ChatMessageResponse;
import com.acentria.benslist.response.ChatPostResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private String user_id, receiver_id, post_id, userName, account_name;
    private RecyclerView rv_recyclerviw;
    private TextView tv_no_records;
    private ImageView iv_send;
    private EditText et_send_massage;
    private ProgressDialog progressDialog;
    private String TAG = ChatHistoryActivity.class.getName();
    private List<ChatMessageResponse> list = new ArrayList<>();


    private boolean is_chat_byer = false;
    private ChatMessageAdatper adapter;
    private Socket socket;
    private boolean mTyping = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Chat History");
        setContentView(R.layout.activity_chat_history_layout);
        ActionBar actionBar = getSupportActionBar();
        ((ActionBar) actionBar).setDisplayHomeAsUpEnabled(true);
        setweigits();
        socket_init();

    }

    private void socket_init() {
        try {
            /*http://192.168.1.117:1234/*/
            /*live url http://benslist.com:8080/    Local url http://192.168.1.117:1234*/
//            socket = IO.socket("http://192.168.1.117:1234");  /*http://192.168.1.3:3000*/
            socket = IO.socket("http://benslist.com:8080");   /*live url http://benslist.com:8080*/

            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            socket.on("message", onNewMessage);
            socket.on("typing", onTyping);
            socket.on("stop typing", onStopTyping);
            socket.on("login", onLogin);
            socket.connect();

            socket.emit("join", user_id);
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }

        list = new ArrayList<>();
//        rv_recyclerviw = (RecyclerView) findViewById(R.id.rv_recyclerviw);
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
//        rv_recyclerviw.setLayoutManager(mLayoutManager);
//        rv_recyclerviw.setItemAnimator(new DefaultItemAnimator());

        rv_recyclerviw.setLayoutManager(new LinearLayoutManager(ChatHistoryActivity.this));
        adapter = new ChatMessageAdatper(ChatHistoryActivity.this, list);
        rv_recyclerviw.scrollToPosition(list.size() - 1);


        et_send_massage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                rv_recyclerviw.scrollToPosition(list.size() - 1);
                if (actionId == R.id.send || actionId == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });

        et_send_massage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.e(TAG, "onFocusChange: " + hasFocus + " , " + list.size());
                    rv_recyclerviw.scrollToPosition(list.size() - 1);
                }
            }
        });

        /*.......using for onTyping listner .....................*/
        //        et_send_massage.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                rv_recyclerviw.scrollToPosition(list.size() - 1);
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (null == userName) return;
//                if (!socket.connected()) return;
//
//                if (!mTyping) {
//                    mTyping = true;
//                    JSONObject jsonObject = new JSONObject();
//                    try {
//                        jsonObject.put("room_id", room_id);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    Log.e(TAG, "onTextChanged " + "typinng=> " + room_id);
//                    socket.emit("typing", jsonObject);
////                    mInputMessageView.setHint("Typing...");
//
//
//                }
//                /*new line added  */           /*     else {
//                    mInputMessageView.setHint(getResources().getString(R.string.typesms)); }*/
//                mTypingHandler.removeCallbacks(onTypingTimeout);
//                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
        /*....................*/

    }


    private void setweigits() {
        /*implent for chat*/
        if (getIntent().getExtras() != null) {
            if (!getIntent().getStringExtra("account_name").equalsIgnoreCase("Seller")) {
                account_name = getIntent().getStringExtra("account_name");
                user_id = getIntent().getStringExtra("user_id");
                receiver_id = getIntent().getStringExtra("merchant_id");
                post_id = getIntent().getStringExtra("post_id");
                userName = getIntent().getStringExtra("username");
                is_chat_byer = true;
                Log.e(TAG, "setweigits " + " account_name=> " + account_name + "\tuser_id " + user_id + "\treceiver_id " + receiver_id + "\tpost_id " + post_id + "username marchent" + "\t ischat bayer " + is_chat_byer);

            } else {
                account_name = getIntent().getStringExtra("account_name");
                user_id = getIntent().getStringExtra("user_id");
                receiver_id = getIntent().getStringExtra("merchant_id");
                post_id = getIntent().getStringExtra("post_id");
                userName = getIntent().getStringExtra("username");
                is_chat_byer = false;
                Log.e(TAG, "setweigits " + " account_name=> " + account_name + "\tuser_id " + user_id + "\treceiver_id " + receiver_id + "\tpost_id " + post_id + "username marchent" + "\t ischat bayer " + is_chat_byer);


            }
        }
        et_send_massage = findViewById(R.id.et_send_massage);
        iv_send = findViewById(R.id.iv_send);
        iv_send.setOnClickListener(this);
        rv_recyclerviw = findViewById(R.id.rv_recyclerviw);

        tv_no_records = findViewById(R.id.tv_no_records);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading...");

        if (Utils.isOnline(this)) {
//            call_chat_historyApi();
        } else {
            rv_recyclerviw.setVisibility(View.GONE);
            tv_no_records.setVisibility(View.VISIBLE);
            tv_no_records.setText(getResources().getString(R.string.network_connection_error));
        }

    }

    private void call_chat_historyApi() {

        if (user_id == null) {
            return;
        }
        if (receiver_id == null) {
            return;
        }
        if (post_id == null) {
            return;
        }


        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", user_id)
                .addFormDataPart("merchant_id", receiver_id)
                .addFormDataPart("post_id", post_id)
                .build();
        Request request = new Request.Builder()
                .url("https://www.benslist.com/Api/Chat/chat_post_list_message_load.inc.php")
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
                        Toast.makeText(ChatHistoryActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
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

                                for (int i = 0; i < list.size(); i++) {
//                                    list.get(i).setMerchentName(usernameformarchent);
                                    Log.e(TAG, "onResponse: set merchent name for chat history" + list.get(i).getUser_name());
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        tv_no_records.setVisibility(View.GONE);
                                        adapter = new ChatMessageAdatper(ChatHistoryActivity.this, list);
                                        rv_recyclerviw.setLayoutManager(new LinearLayoutManager(ChatHistoryActivity.this));
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
//                if (et_send_massage.getText().length() > 0) {
//                    Log.e(TAG, "onClick: no message type than can't be send button");
//                    if (Utils.isOnline(this)) {
//                        call_sendmessage_Api(et_send_massage.getText().toString());
//                        Utils.hideKeyboard(et_send_massage);
//                    } else {
//                        Toast.makeText(ChatHistoryActivity.this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
//                    }
//                } else {
//                    Log.e(TAG, "onClick: no message type than can't be send button" + et_send_massage.getText().toString());
//                }
                attemptSend();
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
            } else {
                SEND_MESSAGE = "merchant_message";
            }
        }


        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
//                .addFormDataPart("user_id", user_id)
//                .addFormDataPart("merchant_id", merchant_id)
                .addFormDataPart("sender_id", user_id)
                .addFormDataPart("receiver_id", receiver_id)
                .addFormDataPart("post_id", post_id)
                .addFormDataPart(SEND_MESSAGE, send_message)
                .build();

        Request request = new Request.Builder()
                .url("https://www.benslist.com/Api/Chat/chat_message.inc.php")
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
                        Toast.makeText(ChatHistoryActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
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
                                        chatpojo.setUserName(userName);
                                        chatpojo.setDate(newlist.get(0).getDate());
                                        chatpojo.setMerchantId(newlist.get(0).getMerchantId());
                                        chatpojo.setUserId(newlist.get(0).getUserId());
                                        chatpojo.setUserMessage(newlist.get(0).getUserMessage());
                                        chatpojo.setMerchantMessage(newlist.get(0).getMerchantMessage());
                                        list.add(chatpojo);

                                        Log.e(TAG, "run: list size atfer set pojo " + list.size());
                                        if (list.size() > 0) {
                                            if (list.size() == 1) {
                                                rv_recyclerviw.setVisibility(View.VISIBLE);
                                                adapter = new ChatMessageAdatper(ChatHistoryActivity.this, list/*, is_chat_byer*/);
                                                rv_recyclerviw.setLayoutManager(new LinearLayoutManager(ChatHistoryActivity.this));
                                                rv_recyclerviw.setAdapter(adapter);
                                                adapter.notifyDataSetChanged();
                                                Log.e(TAG, "run: send frist massage show in adatper " + list.size());
                                            } else {
                                                adapter = new ChatMessageAdatper(ChatHistoryActivity.this, list/*, is_chat_byer*/); /**/
                                                adapter.notifyItemInserted(list.size() - 1);
                                                rv_recyclerviw.scrollToPosition(adapter.getItemCount() - 1);
                                                Log.e(TAG, "run: send frist massage show in at last possition on adatper " + list.size());

                                            }
                                        }


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


    /*Chat socket method Approch*/


    private void attemptSend() {

        if (!socket.connected()) {
            Toast.makeText(ChatHistoryActivity.this, "network error", Toast.LENGTH_LONG).show();
            Log.e(TAG, "onClick: network error");
        } else {
//                    String message = StringEscapeUtils.escapeJava(messagetxt.getText().toString().trim());
            String messageType = et_send_massage.getText().toString().trim();


            //retrieve the nickname and the message content and fire the event messagedetection
            if (TextUtils.isEmpty(messageType)) {
                return;
            }
            long message_time = System.currentTimeMillis();
            String imgUrl = Account.accountData.get("photo");

//                    socket.emit("messagedetection", Nickname, messagetxt.getText().toString());
            addMessage(userName, messageType, true, message_time, imgUrl);


            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("receiver_id", receiver_id);
                jsonObject.put("sender_id", user_id);
                jsonObject.put("user_name", Account.accountData.get("username"));
                jsonObject.put("message", messageType);
                jsonObject.put("post_id", post_id);
                jsonObject.put("message_time", message_time);
                jsonObject.put("img_url", imgUrl);

                socket.emit("newMessage", jsonObject);

                Log.e(TAG, "send to socket: " + jsonObject);
                et_send_massage.setText(" ");
                adapter.notifyItemInserted(list.size() - 1);
                scrollToBottom();
//                        jsonObject.put("room_id", room_id)
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "onConnect");
                    /*new implement*/

                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("receiver_id", receiver_id);
                        jsonObject.put("sender_id", user_id);
                        socket.emit("add user", jsonObject);
                        Log.e(TAG, "add user-:" + jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "onDisconnect");
                }
            });
        }
    };
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "onConnectError");
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    String reciver_id, reciver_name, message, imgUrl;
                    try {
                        Log.d(TAG, "onNewMessage" + data.toString());
//                        JSONObject senderjson = data.getJSONObject("senderNickname");
                        JSONObject senderjson = data.getJSONObject("chatdata");

                        Log.e(TAG, "sender_json : " + senderjson);

                        Log.e(TAG, "sender id : " + senderjson.getString("sender_id"));
                        Log.e(TAG, "receiver id : " + senderjson.getString("receiver_id"));
                        Log.e(TAG, "user_name  : " + senderjson.getString("user_name"));
                        Log.e(TAG, "message: " + senderjson.getString("message"));

                        /*IsMine=true*/
//                        reciver_name = senderjson.getString("sender_id");
                        reciver_name = senderjson.getString("user_name");
                        message = senderjson.getString("message");
                        imgUrl = senderjson.getString("img_url");

                        long message_time = System.currentTimeMillis();
                        addMessage(reciver_name, message, false, message_time, imgUrl);

                    } catch (JSONException e) {
                        Log.e(TAG, "Exception in Chat=>" + e.getMessage());
                        return;
                    }

                }
            });
        }
    };


    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.e(TAG, "onLogin: " + data);
//                        Log.e(TAG, "room-id" + data.getString("room_id"));
//                        room_id = data.getString("room_id");
//                        getMessageList();
                    /*call history Api*/
                }
            });
        }
    };

    /*ontyping and onStoptyping*/

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    String username = "";
                    try {
                        Log.e(TAG, "onTyping: " + data);
                        Log.e(TAG, "onTyping=> " + " isTyping" + data.getString("receiver_id"));
                        et_send_massage.setHint("Typing..." + data.getString("receiver_id"));
//                        messagetxt.setText("typing..." + data.getString("receiver_id"));

                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }


                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("receiver_id");
                        et_send_massage.setText("online");
                        et_send_massage.setHint("type message");
                        Log.e(TAG, "onStopTyping than typing off" + data);

                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                }
            });
        }
    };


    private void addMessage(String reciver_name, String message, boolean isMine, long message_time, String imgUrl) {

        Log.e(TAG, "addMessage: isMine " + isMine);
        Log.e(TAG, "addMessage: imgUrl " + imgUrl);
        Log.e(TAG, "attemptSend message time: " + message_time);
//        ChatMessageResponse m = new ChatMessageResponse(reciver_name, message, isMine);
        ChatMessageResponse chatMessageResponse = new ChatMessageResponse();
        chatMessageResponse.setUserName(reciver_name);
        chatMessageResponse.setMessage(message);
        chatMessageResponse.setMine(isMine);
        chatMessageResponse.setMessage_time(message_time);
        chatMessageResponse.setImg_url(imgUrl);
        //add the message to the messageList
        list.add(chatMessageResponse);
        // add the new updated list to the dapter
//        adapter = new ChatMessageAdatper(this, list, isMine);
        // notify the adapter to update the recycler view
//        adapter.notifyDataSetChanged();

        //set the adapter for the recycler view
        rv_recyclerviw.setAdapter(adapter);
//        Toast.makeText(ChatBoxProActivity.this, reciver_name + "\n" + message, Toast.LENGTH_LONG).show();

        adapter.notifyItemInserted(list.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        rv_recyclerviw.scrollToPosition(adapter.getItemCount() - 1);
    }

}
