package com.acentria.benslist.chatprocess;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.acentria.benslist.Account;
import com.acentria.benslist.Config;
import com.acentria.benslist.Lang;
import com.acentria.benslist.R;
import com.acentria.benslist.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatHistoryReportActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "ChatHistoryReportActivity=> ";
    RadioGroup rd_group;
    RadioButton rd_Inappropriate_content, rd_bullying, rd_racism;
    Button btn_report;
    private String marchentid = "", post_id = "", reason = "";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Chat History Report");
        setContentView(R.layout.dialog_report_chat_hirtory_layout);
        /* enable back action */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (getIntent().getExtras() != null) {
            marchentid = getIntent().getStringExtra("marchentid");
            post_id = getIntent().getStringExtra("post_id");
        }
        btn_report = findViewById(R.id.btn_report);
        rd_Inappropriate_content = findViewById(R.id.rd_Inappropriate_content);
        rd_bullying = findViewById(R.id.rd_bullying);
        rd_racism = findViewById(R.id.rd_racism);
        rd_group = findViewById(R.id.rd_group);
        rd_group.setOnCheckedChangeListener(this);
        btn_report.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading...");


    }

    @SuppressLint("LongLogTag")
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int selectedId = rd_group.getCheckedRadioButtonId();

        if (selectedId != -1) {
            Log.e(TAG, "onCheckedChanged: !=-1  " + selectedId);
            RadioButton radioButton = (RadioButton) findViewById(selectedId);

            if (radioButton.getText().toString().equalsIgnoreCase("Inappropriate_content")) {
                reason = radioButton.getText().toString();
                Log.e(TAG, "onCheckedChanged: " + reason);
            } else if (radioButton.getText().toString().equalsIgnoreCase("bullying")) {
                reason = radioButton.getText().toString();
                Log.e(TAG, "onCheckedChanged: " + reason);
            } else if (radioButton.getText().toString().equalsIgnoreCase("racism")) {
                reason = radioButton.getText().toString();
                Log.e(TAG, "onCheckedChanged: " + reason);
            }
        } else {
            Log.e(TAG, "onCheckedChanged: -1 " + selectedId);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_report:
                call_chatreportApi();

                break;
        }
    }

    private void call_chatreportApi() {

        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uid", Account.accountData.get("id"))
                .addFormDataPart("mid", marchentid)
                .addFormDataPart("post_id", post_id)
                .addFormDataPart("reason", "")
                .build();

        Request request = new Request.Builder()
                /* .url(BASE_URL + route)*/
                .url("https://www.benslist.com/sendReportApi.inc.php")
                .post(requestBody)
                .build();
        progressDialog.show();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Log.e(TAG, "run: onfailure");
                        Toast.makeText(ChatHistoryReportActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String mresponse = response.body().string();
                    final JSONObject jsonObject = new JSONObject(mresponse);
                    final String status = jsonObject.getString("success");
                    runOnUiThread(new Runnable() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if (status.equalsIgnoreCase("success")) {
                                Utils.isAlertBox(ChatHistoryReportActivity.this, "", "Report has been send");
//                                Config.alertDailog(ChatHistoryReportActivity.this, "Report has been send");
                                Log.e(TAG, "status success: " + status);
                            }


                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


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

}
