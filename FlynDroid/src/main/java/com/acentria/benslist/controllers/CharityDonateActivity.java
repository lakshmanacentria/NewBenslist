package com.acentria.benslist.controllers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.acentria.benslist.Account;
import com.acentria.benslist.Config;
import com.acentria.benslist.Dialog;
import com.acentria.benslist.Lang;
import com.acentria.benslist.PurchaseActivity;
import com.acentria.benslist.R;
import com.acentria.benslist.SwipeMenu;
import com.acentria.benslist.Utils;
import com.acentria.benslist.adapters.CharityDonateAdapter;
import com.acentria.benslist.adapters.ProductqunatiyAdapter;
import com.acentria.benslist.chatprocess.ChatinPostActivity;
import com.acentria.benslist.response.City;
import com.acentria.benslist.response.Country;
import com.acentria.benslist.response.DonateCharityResponse;
import com.acentria.benslist.response.State;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CharityDonateActivity extends AppCompatActivity implements ProductqunatiyAdapter.onClickQuanity, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final int PAYPAL_REQUEST_CODE = 123;
    private String TAG = "CharityDonateActivity=>";
    private String ref_no, mrs_str = "", country_str = "", state_str = "", city_str = "", ammount_withoutgood_str = "",
            fristname = "", lastname = "", email_str = "", telephone = "", address = "", keepme_str = "", charity_id = "", payer_email = "", residence_country = "";

    private TextView tv_total_title, tv_remaingtitle;
    private Button btn_submit;
    private LinearLayout ll_total, ll_remainingl, ll_product_qauntity;
    private EditText et_fristname, et_lasttname, et_email, et_tel, et_address, et_amount_without_gods;
    private Spinner mrs_spinner, spinner_selectcountry, spinner_select_state, spinner_select_city;
    private ArrayList<Country> countries = new ArrayList<>();
    private ArrayList<State> states = new ArrayList<>();
    private ArrayList<City> cities = new ArrayList<>();
    private ArrayList<String> mrlist = new ArrayList<>();


    private ArrayAdapter<String> mrs_arrayAdapter;
    private ArrayAdapter<Country> countryArrayAdapter;
    private ArrayAdapter<State> stateArrayAdapter;
    private ArrayAdapter<City> cityArrayAdapter;

    private RecyclerView rv_productquanity;
    private CheckBox chk_post, chk_mobile, chk_email;
    private RadioButton radio_paypal;
    private LinearLayout ll_paypall;
    private ProgressDialog progressDialog;
    private ProductqunatiyAdapter adatper;
    List<DonateCharityResponse> list = new ArrayList<>();

    static String env_mode = Utils.getCacheConfig("android_paypal_sandbox").equals("1") ? PayPalConfiguration.ENVIRONMENT_SANDBOX : PayPalConfiguration.ENVIRONMENT_PRODUCTION;

    //Paypal Configuration Object
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);
    private String paymentAmount = "";
    private String mproduct = "", mqunatity = "";
    private String is_fromside = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Charity Donate Activity");
        setContentView(R.layout.activity_layout_charity_donate);
        ActionBar actionBar = getSupportActionBar();
        ((ActionBar) actionBar).setDisplayHomeAsUpEnabled(true);
        ll_paypall = findViewById(R.id.ll_paypall);
        if (getIntent().getExtras() != null) {
            ref_no = getIntent().getStringExtra("Ref_no");
            if (getIntent().getStringExtra("is_foodside") != null) {
                setTitle("Food Donate Activity");
                ll_paypall.setVisibility(View.GONE);
                is_fromside = getIntent().getStringExtra("is_foodside");
            }
            Log.e(TAG, "RefNO " + ref_no);
        }
        inititewigits();
        Intent intent = new Intent(this, PayPalService.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        startService(intent);
    }

    private void inititewigits() {
        tv_total_title = findViewById(R.id.tv_total_title);
        tv_remaingtitle = findViewById(R.id.tv_remaingtitle);
        btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
        ll_total = findViewById(R.id.ll_total);
        ll_remainingl = findViewById(R.id.ll_remaining);
        ll_product_qauntity = findViewById(R.id.ll_product_qauntity);

        et_fristname = findViewById(R.id.et_fristname);
        et_lasttname = findViewById(R.id.et_lasttname);
        et_email = findViewById(R.id.et_email);
        et_tel = findViewById(R.id.et_tel);
        et_address = findViewById(R.id.et_address);
        rv_productquanity = findViewById(R.id.rv_productquanity);
        et_amount_without_gods = findViewById(R.id.et_amount_without_gods);

        chk_post = findViewById(R.id.chk_post);
        chk_mobile = findViewById(R.id.chk_mobile);
        chk_email = findViewById(R.id.chk_email);
        chk_post.setOnCheckedChangeListener(this);
        chk_mobile.setOnCheckedChangeListener(this);
        chk_email.setOnCheckedChangeListener(this);
        radio_paypal = findViewById(R.id.radio_paypal);
        radio_paypal.setOnClickListener(this);

        mrs_spinner = findViewById(R.id.mrs_spinner);
        spinner_selectcountry = findViewById(R.id.spinner_selectcountry);
        spinner_select_state = findViewById(R.id.spinner_select_state);
        spinner_select_city = findViewById(R.id.spinner_select_city);

        /*onItemsleectedListner On Spinners*/
        mrlist.add("Mr.");
        mrlist.add("Mrs.");
        mrlist.add("others");
        spinner_selectcountry.setOnItemSelectedListener(country_listener);
        spinner_select_state.setOnItemSelectedListener(state_listener);
        spinner_select_city.setOnItemSelectedListener(city_listener);
        mrs_spinner.setOnItemSelectedListener(mrs_listener);
        mrs_arrayAdapter = new ArrayAdapter<String>(CharityDonateActivity.this, R.layout.simple_spinner_dropdown_item, mrlist);
        mrs_spinner.setAdapter(mrs_arrayAdapter);

        /*progress dialog inilisize*/
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading...");
        if (Utils.isOnline(this)) {
            callCharityDetailApi();
        } else {
            Toast.makeText(this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
        }
        Utils.hideKeyboard(ll_product_qauntity);


        /*paypal account intilize */
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
        Log.e(TAG, "env_mode=>" + env_mode);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }


    /*OnAdapterView OnitemsSelectedListner object creates*/
    private AdapterView.OnItemSelectedListener mrs_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mrs_str = mrs_spinner.getItemAtPosition(position).toString();
            Log.e(TAG, "mrs=> " + mrs_str);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private AdapterView.OnItemSelectedListener country_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long llong) {
            if (position > 0) {
                Country country = (Country) spinner_selectcountry.getItemAtPosition(position);
                country_str = country.getCountryName();
                if (Utils.isOnline(CharityDonateActivity.this)) {
                    call_StateApi(country.getCountryName());
                } else {
                    Toast.makeText(CharityDonateActivity.this, "Make sure your device connect to internet", Toast.LENGTH_LONG).show();
                }
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
    private AdapterView.OnItemSelectedListener state_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long llong) {
            if (position > 0) {
                State state = (State) spinner_select_state.getItemAtPosition(position);
                state_str = state.getStateName();
                Log.e("SpinnerSates", "onItemSelected: country: " + state.getStateName() + " " + state.getCountryName());
                if (Utils.isOnline(CharityDonateActivity.this)) {
                    call_cityApi(state.getStateName(), state.getCountryName());
                } else {
                    Toast.makeText(CharityDonateActivity.this, "make sure your device connect to internet", Toast.LENGTH_LONG).show();

                }
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
    private AdapterView.OnItemSelectedListener city_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long llong) {
            if (position > 0) {
                final City city = (City) spinner_select_city.getItemAtPosition(position);
                city_str = city.getCityName();
                Log.e(TAG, "City OnItemSelectedListener" + "selectcity " + city_str);

            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    /*call Country Api for using to set value on spinner*/
    /*call Country Api and set date on country spinner */
    private void callCountryListApi() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("https://www.benslist.com/Api/get_location.inc.php").build();
        progressDialog.show();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure:");
                Toast.makeText(CharityDonateActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json_response = response.body().string();
                    Log.e(TAG, "onResponse: \n" + json_response);

                    if (!json_response.equalsIgnoreCase("[]")) {
                        try {
                            JSONArray itemArray = new JSONArray(json_response);

                            Type type = new TypeToken<ArrayList<Country>>() {
                            }.getType();
                            countries = (new Gson()).fromJson(itemArray.toString(), type);
                            for (int i = 0; i < countries.size(); i++) {
                                if (countries.get(i).getCountryName().equalsIgnoreCase("")) {
                                    countries.get(i).setCountryName("Please Select Country");
                                }
                            }

                            Log.e(TAG, "FilerCountryArrayBefor pass in spiner adapter " + countries.get(0).getCountryName());
                            countryArrayAdapter = new ArrayAdapter<Country>(CharityDonateActivity.this, R.layout.simple_spinner_dropdown_item, countries);
                            CharityDonateActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    spinner_selectcountry.setAdapter(countryArrayAdapter);
                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        progressDialog.dismiss();
                        Log.e(TAG, "Country response error and response [] blank get ");
                    }
                } else {
                    progressDialog.dismiss();
                    Log.e(TAG, "Country response unscuess fully ");
                }


            }
        });
    }

    /*call state Api and set data on  state spinner*/
    private void call_StateApi(final String countryName) {


        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("countryname", countryName)
                .build();

        Request request = new Request.Builder()
                /* .url(BASE_URL + route)*/
                .url("https://www.benslist.com/Api/" + "get_location.inc.php")
                .post(requestBody)
                .build();
        progressDialog.show();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "State OnFailure " + "onFailure:");
//                Toast.makeText(CharityDonateActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json_response = response.body().string();
                    Log.e(TAG, "onResponse: \n" + json_response);

                    if (!json_response.equalsIgnoreCase("[]")) {
                        try {

                            JSONArray itemArray = new JSONArray(json_response);
                            Type type = new TypeToken<ArrayList<State>>() {
                            }.getType();
                            states = (new Gson()).fromJson(itemArray.toString(), type);

                            for (int cAs = 0; cAs < states.size(); cAs++) {
                                states.get(cAs).setCountryName(countryName);
                                Log.e(TAG, "Implement country in State pojo " + states.get(cAs).getCountryName());

                            }

                            state_str = states.get(0).getStateName();
                            State statepojo = new State(); /*add static value in pojo*/
                            statepojo.setStateName("Please Select State");
                            states.add(0, statepojo);/*upade value on first poition of arrarylist*/
                            Log.e(TAG, "FilerStateArray Befor pass in spiner adapter " + states.get(0).getCountryName() + "\nState name" + state_str);
                            stateArrayAdapter = new ArrayAdapter<State>(CharityDonateActivity.this, R.layout.simple_spinner_dropdown_item, states);
                            CharityDonateActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    spinner_select_state.setAdapter(stateArrayAdapter);
                                    /*call state api for for first set spinner state wise set city befor selected onItemsselect lisners*/
//                                    call_cityApi(states.get(0).getStateName(), states.get(0).getCountryName());

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        progressDialog.dismiss();
                        Log.e(TAG, "State return [] arrary");
                    }

                } else {
                    progressDialog.dismiss();
                    Log.e(TAG, "State Unsuccess  and return [] arrary");
                }

            }
        });


    }

    /*call city Api and set date on city spinner */
    private void call_cityApi(String stateName, String countryName) {

        OkHttpClient okHttpClientforcity = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("countryname", countryName)
                .addFormDataPart("statename", stateName)
                .build();

        Request requestforcity = new Request.Builder()
                /* .url(BASE_URL + route)*/
                .url("https://www.benslist.com/Api/get_location.inc.php")
                .post(requestBody)
                .build();
        progressDialog.show();
        okHttpClientforcity.newCall(requestforcity).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
                Log.e(TAG, "city onFailure ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        Log.e(TAG, "city successfully ");

                        String json_response = response.body().string();
                        Log.e(TAG, "onResponse: \n" + json_response);
                        if (!json_response.equalsIgnoreCase("[]")) {
                            JSONArray cityitemArray = new JSONArray(json_response);
                            Type type = new TypeToken<ArrayList<City>>() {
                            }.getType();
                            cities = (new Gson()).fromJson(cityitemArray.toString(), type);
                            city_str = cities.get(0).getCityName();

                            City citypojo = new City();
                            citypojo.setCityName("Please Select City");
                            cities.add(0, citypojo);
                            cityArrayAdapter = new ArrayAdapter<City>(CharityDonateActivity.this, R.layout.simple_spinner_dropdown_item, cities);
                            CharityDonateActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    spinner_select_city.setAdapter(cityArrayAdapter);

                                }
                            });


                        } else {
                            progressDialog.dismiss();
                            Log.e(TAG, "City Unsuccess  and return [] arrary");
                        }


                    } else {
                        progressDialog.dismiss();
                        Log.e(TAG, "City Unsuccess ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void callCharityDetailApi() {
        Log.e(TAG, "refno " + ref_no);
        if (ref_no == null) {
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("ref_number", ref_no)
                .build();

        final Request request = new Request.Builder()
                /* .url(BASE_URL + route)*/
                .url("https://www.benslist.com/Api/charity_details.inc.php")
                .post(requestBody)
                .build();
        progressDialog.show();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
                Log.e(TAG, "failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String mresponse = response.body().string();
                    Log.e(TAG, "Resonse=> " + mresponse);
                    if (!mresponse.equalsIgnoreCase("[]")) try {
                        JSONArray itemArray = new JSONArray(mresponse);
                        final JSONObject jsonObject = itemArray.getJSONObject(0);
                        Type type = new TypeToken<List<DonateCharityResponse>>() {
                        }.getType();
                        list = (new Gson()).fromJson(itemArray.toString(), type);
                        charity_id = list.get(0).getId();
                        payer_email = list.get(0).getEmail();
                        residence_country = list.get(0).getCountry();

                        Log.e(TAG, "jsonObject pos 0" + jsonObject);
                        Log.e(TAG, jsonObject.getString("title"));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                setweightsdatafromdynamically(list);
                                if (list.get(0).getAmount().equalsIgnoreCase("0")) {
                                    rv_productquanity.setVisibility(View.VISIBLE);
                                    rv_productquanity.setLayoutManager(new LinearLayoutManager(CharityDonateActivity.this));
                                    adatper = new ProductqunatiyAdapter(list, CharityDonateActivity.this, CharityDonateActivity.this);
                                    rv_productquanity.setAdapter(adatper);
                                    adatper.notifyDataSetChanged();
                                }


                            }
                        });


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    else {
                        progressDialog.dismiss();
                        Log.e(TAG, "reponse [] arary return" + mresponse);
                    }
                } else {
                    progressDialog.dismiss();
                    Log.e(TAG, "Unsucess");

                }
            }
        });


    }

    private void setweightsdatafromdynamically(List<DonateCharityResponse> list) {
        if (list.get(0).getAmount().equalsIgnoreCase("0")) {
            tv_total_title.setText("Total Goods");
            tv_remaingtitle.setText("Remaining Goods");
            et_amount_without_gods.setVisibility(View.GONE);
            rv_productquanity.setVisibility(View.VISIBLE);
            Log.e(TAG, "ammount 0 means goods side" + list.get(0).getAmount());
        } else {
            tv_total_title.setText("Total Amount");
            tv_remaingtitle.setText("Remaining Amount");
            et_amount_without_gods.setVisibility(View.VISIBLE);
            rv_productquanity.setVisibility(View.GONE);
            Log.e(TAG, "ammount Not means Amount side" + list.get(0).getAmount());
        }
        for (int i = 0; i < list.size(); i++) {
            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            TextView tv_total = new TextView(this);
            TextView tv_remaining = new TextView(this);
//            tv_total.setTypeface(null, Typeface.BOLD);
            tv_total.setTextSize(16);
            tv_total.setTextColor(Color.BLACK);
//            tv_remaining.setTypeface(null, Typeface.BOLD);
            tv_remaining.setTextSize(16);
            tv_remaining.setTextColor(Color.BLACK);
            tv_total.setLayoutParams(lparams);
            tv_remaining.setLayoutParams(lparams);
            if (list.get(i).getAmount().equalsIgnoreCase("0")) {
                tv_total.setText(list.get(i).getProduct() + " : " + list.get(i).getQuantity());
                tv_remaining.setText(list.get(i).getProduct() + " : " + list.get(i).getQuantityRemain());
            } else {
                tv_total.setText("$" + list.get(i).getAmount());
                tv_remaining.setText("$" + list.get(i).getRemainingAmount());
            }


            this.ll_total.addView(tv_total);
            this.ll_remainingl.addView(tv_remaining);
            Log.e(TAG, "list" + list.get(i).getImage());

        }


        /*Call country picker Api After response the charity detial api*/
        if (Utils.isOnline(this)) {
            callCountryListApi();
        } else {

            Toast.makeText(CharityDonateActivity.this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onItemClick(int posi, String product, String qunatity) {
        Log.e(TAG, "onItemClick=> " + posi + " \tProdct " + product + " \t " + qunatity);
        mproduct = product;
//        mqunatity = qunatity;

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
            case R.id.btn_submit:
                if (getIntent().getStringExtra("is_foodside") != null) {
                    validationcharityDonatefoodApi();
                    Log.e(TAG, "onClick: " + "is_foodside" + getIntent().getStringExtra("is_foodside"));
//                    Toast.makeText(this, "Under Working", Toast.LENGTH_LONG).show();
                } else {
                    validationcharityDonateApi();
                }


                /*got to paypal sdk*/
//                getpaymentwithpaypal();


                break;
            case R.id.radio_paypal:
                if (radio_paypal.isSelected() == false) {
                    radio_paypal.setSelected(true);
                } else {
                    radio_paypal.setSelected(false);
                }
                break;
        }
    }

    private void validationcharityDonatefoodApi() {
        if (!is_validation()) {
            return;
        }

        /*if list is show than goods list send pera*/
        else if (country_str.isEmpty()) {
            Toast.makeText(CharityDonateActivity.this, "please select country.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "country empty fields is request " + country_str);
        } else if (country_str.equalsIgnoreCase("Please Select Country")) {
            Toast.makeText(CharityDonateActivity.this, "please select country.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "country fields is request " + country_str);
        } else if (state_str.isEmpty()) {
            Toast.makeText(CharityDonateActivity.this, "please select state.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "state fields is request " + state_str);
        } else if (city_str.isEmpty()) {
            Toast.makeText(CharityDonateActivity.this, "please select city.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "city fields is request " + city_str);
        } else if (keepme_str.isEmpty()) {
            Toast.makeText(this, "Please check Keep Me up to date on sailors society news", Toast.LENGTH_LONG).show();
            Log.e(TAG, "keeepme " + keepme_str);
        } /*else if (adatper.mlist.get(0).getManualQuanty().isEmpty()) {
            Toast.makeText(this, "Qaunity filed is required", Toast.LENGTH_LONG).show();

        }*/ else {
            if (Utils.isOnline(this)) {
                /*paypal for payment getway than after call api*/
                call_charitydonat_api("", "", "", "", "");
            } else {
                Toast.makeText(this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
            }
        }

    }


    private void validationcharityDonateApi() {
        if (!is_validation()) {
            return;
        }/*if list is show than goods list send pera*/ else if (country_str.isEmpty()) {
            Toast.makeText(CharityDonateActivity.this, "please select country.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "country empty fields is request " + country_str);
        } else if (country_str.equalsIgnoreCase("Please Select Country")) {
            Toast.makeText(CharityDonateActivity.this, "please select country.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "country fields is request " + country_str);
        } else if (state_str.isEmpty()) {
            Toast.makeText(CharityDonateActivity.this, "please select state.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "state fields is request " + state_str);
        } else if (city_str.isEmpty()) {
            Toast.makeText(CharityDonateActivity.this, "please select city.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "city fields is request " + city_str);
        } else if (keepme_str.isEmpty()) {
            Toast.makeText(this, "Please check Keep Me up to date on sailors society news", Toast.LENGTH_LONG).show();
            Log.e(TAG, "keeepme " + keepme_str);
        } else if (radio_paypal.isSelected() == false) {
            Toast.makeText(this, "Please select PAYMENT GATEWAYS", Toast.LENGTH_LONG).show();
        } else {
            if (Utils.isOnline(this)) {
                /*paypal for payment getway than after call api*/
                getpaymentwithpaypal();
            } else {
                Toast.makeText(this, getResources().getString(R.string.network_connection_error), Toast.LENGTH_LONG).show();
            }
        }

    }

    private void call_charitydonat_api(String paymentdate, String id, String state_payment_status, String bussinesstype, String response_type) {
        String payer_id = Account.accountData.get("id");
        Log.e(TAG, " befor sending Data on server=> " + payer_id + "\n" + ref_no + mrs_str + "\n" + country_str + "\n" + state_str + "\n" + city_str + "\n" + ammount_withoutgood_str +
                "\n" + fristname + lastname + "\n" + email_str + "" + telephone + "\n" + address + "\n" + keepme_str + "charity id" + charity_id);
//        mqunatity = adatper.mlist.get(0).getManualQuanty();
//        Log.e(TAG, "call_charitydonat_api: " + mqunatity);


        for (int i = 0; i < list.size(); i++) {
            final String fillQauntity = list.get(i).getManualQuanty();
            final String totalQauntity = list.get(i).getQuantity();
            if (fillQauntity.equalsIgnoreCase("")) {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(CharityDonateActivity.this, "please fill qauntity" + totalQauntity, Toast.LENGTH_LONG);
                       Log.e(TAG, "call_charitydonat_api: please fill qauntity=> " + fillQauntity);
                   }
               });

                return;
            }
            double fillQaunt = Double.valueOf(fillQauntity);
            double totalQaunt = Double.valueOf(totalQauntity);

            Log.e(TAG, "call_charitydonat_api: list " + list.get(i));
            Log.e(TAG, "call_charitydonat_api: Quantity=> " + list.get(i).getQuantity());
            Log.e(TAG, "call_charitydonat_api: ManualQauntity=> " + list.get(i).getManualQuanty());
            if (fillQaunt > totalQaunt) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CharityDonateActivity.this, "Total  qauntity is " + totalQauntity, Toast.LENGTH_LONG);
                        return;
                    }
                });
            }
        }

        OkHttpClient okHttpCharityDonate = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
//        builder.addFormDataPart("charity_id", charity_id)
//                .addFormDataPart("first_name", fristname)
//                .addFormDataPart("last_name", lastname)
//                .addFormDataPart("email", email_str)
//                .addFormDataPart("telephone", telephone)
//                .addFormDataPart("country", country_str)
//                .addFormDataPart("state", state_str)
//                .addFormDataPart("city", city_str)
//                .addFormDataPart("keepme", keepme_str)
//                .addFormDataPart("amount", ammount_withoutgood_str)
//                .addFormDataPart("payer_email", email_str)
//                .addFormDataPart("residence_country", residence_country)
//                .addFormDataPart("txn_id", id)
//                .addFormDataPart("mc_currency", "$")
//                .addFormDataPart("payment_gross", "211")
//                .addFormDataPart("payment_fee", "123")
//                .addFormDataPart("payment_status", state_payment_status)
//                .addFormDataPart("item_name", "abc")
//                .addFormDataPart("payment_date", paymentdate)
//                .addFormDataPart("business", bussinesstype)

                /*.addFormDataPart("c_type", "F")
                .addFormDataPart("[product]", product)
                .addFormDataPart("[quantity]", qauntity);*/
        String url = "";

        if (getIntent().getStringExtra("is_foodside") != null) {
            if (list.get(0).getQuantity().equalsIgnoreCase("")) {
                Toast.makeText(CharityDonateActivity.this, "Please enter qauntity", Toast.LENGTH_LONG).show();
                return;
            }
            if (ref_no == null) {
                Toast.makeText(CharityDonateActivity.this, "Ref number not found.", Toast.LENGTH_LONG).show();
                return;
            }

            url = "https://www.benslist.com/Api/food_donate.inc.php";
            builder.addFormDataPart("charity_id", charity_id)
                    .addFormDataPart("gender_type", mrs_str)
                    .addFormDataPart("first_name", fristname)
                    .addFormDataPart("last_name", lastname)
                    .addFormDataPart("email", email_str)
                    .addFormDataPart("telephone", telephone)
                    .addFormDataPart("country", country_str)
                    .addFormDataPart("state", state_str)
                    .addFormDataPart("city", city_str)
                    .addFormDataPart("keepme", keepme_str)
                    .addFormDataPart("charity_id", ref_no);
//                    .addFormDataPart("[product]", mproduct)
//                    .addFormDataPart("[quantity]", "12");

            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getManualQuanty() != null) {
                        if (!list.get(0).getManualQuanty().equalsIgnoreCase("")) {
                            builder.addFormDataPart("product[" + i + "]", list.get(i).getProduct());
                            builder.addFormDataPart("quantity[" + i + "]", list.get(i).getManualQuanty());
                        }
                    }

                }
            }


        } else {

            url = "https://www.benslist.com/Api/charity_donate.inc.php";
            builder.addFormDataPart("charity_id", charity_id)
                    .addFormDataPart("first_name", fristname)
                    .addFormDataPart("last_name", lastname)
                    .addFormDataPart("email", email_str)
                    .addFormDataPart("telephone", telephone)
                    .addFormDataPart("country", country_str)
                    .addFormDataPart("state", state_str)
                    .addFormDataPart("city", city_str)
                    .addFormDataPart("keepme", keepme_str)
                    .addFormDataPart("amount", ammount_withoutgood_str)
                    .addFormDataPart("payer_email", email_str)
                    .addFormDataPart("payer_id", payer_id)
//                    .addFormDataPart("residence_country", country_str)
                    .addFormDataPart("txn_id", id)
//                    .addFormDataPart("mc_currency", "$")
//                    .addFormDataPart("payment_gross", "211")
//                    .addFormDataPart("payment_fee", "123")
//                    .addFormDataPart("payment_status", state_payment_status)
//                    .addFormDataPart("item_name", "abc")
//                    .addFormDataPart("payment_date", paymentdate)
//                    .addFormDataPart("business", bussinesstype)
            ;
        }
        RequestBody requestBody = builder.build();
        Request requestcharitydonate = new Request.Builder()
                /* .url(BASE_URL + route)*/
                .url(url)
                .post(requestBody)
                .build();
        progressDialog.show();
        okHttpCharityDonate.newCall(requestcharitydonate).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String resp = response.body().string();
                    Log.e(TAG, " response" + resp);
                    if (!resp.equalsIgnoreCase("{}")) {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                try {
                                    JSONObject jsonObject = new JSONObject(resp);
                                    Log.e(TAG, "response status positive " + jsonObject.getString("result"));
                                    if (getIntent().getStringExtra("is_foodside") != null) {
                                        if (is_fromside.equalsIgnoreCase("charitygooddsdonate")) {
                                            Utils.isAlertBox(CharityDonateActivity.this, "", "You’ve successfully donate for charity");

                                        } else {
                                            Utils.isAlertBox(CharityDonateActivity.this, "", "You’ve successfully donate for Food");

                                        }
                                    } else {
                                        Utils.isAlertBox(CharityDonateActivity.this, "", "You’ve successfully donate for charity");
                                    }

//                                    Toast.makeText(CharityDonateActivity.this, jsonObject.getString("result"), Toast.LENGTH_LONG).show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                    } else {
                        progressDialog.dismiss();
                        Log.e(TAG, "respnse {} object return " + resp);
                    }

                } else {
                    progressDialog.dismiss();
                    Log.e(TAG, "not sucessfully");
                }

            }
        });


    }

    private boolean is_validation() {
        boolean valid = true;
        ammount_withoutgood_str = et_amount_without_gods.getText().toString().trim();
        fristname = et_fristname.getText().toString().trim();
        lastname = et_lasttname.getText().toString().trim();
        email_str = et_email.getText().toString().trim();
        telephone = et_tel.getText().toString().trim();
        address = et_address.getText().toString().trim();


        if (et_amount_without_gods.getVisibility() == View.VISIBLE) {
            if (ammount_withoutgood_str.isEmpty()) {
                et_amount_without_gods.setError(getResources().getString(R.string.ammout_filed_error));
                valid = false;
            } else {
                et_amount_without_gods.setError(null);
            }
        }


        if (fristname.isEmpty()) {
            et_fristname.setError(getResources().getString(R.string.firstname_filed_required));
            valid = false;
        } else {
            et_fristname.setError(null);
        }
        if (lastname.isEmpty()) {
            et_lasttname.setError(getResources().getString(R.string.lastname_filed_required));
            valid = false;
        } else {
            et_lasttname.setError(null);
        }
        if (email_str.isEmpty()) {
            et_email.setError(getResources().getString(R.string.email_empty));
            valid = false;
        } else if (!Utils.checkEmail(email_str)) {
            et_email.setError(getResources().getString(R.string.valid_email_error));
            valid = false;
        } else {
            et_email.setError(null);
        }

        if (telephone.isEmpty()) {
            et_tel.setError(getResources().getString(R.string.teli_empty));
            valid = false;
            Log.e(TAG, "teliphone not requried" + telephone);
        } else if (telephone.length() < 7) {
            et_tel.setError(getResources().getString(R.string.tel_minilength_error));
            valid = false;
            Log.e(TAG, "teliphone langth error" + telephone);
        } else {
            et_tel.setError(null);
            Log.e(TAG, "tel match");
        }

        if (address.isEmpty()) {
            et_address.setError(getResources().getString(R.string.add_desc_fields_error));
            valid = false;
            Log.e(TAG, "Address  fields is request " + address);
        } else {
            et_address.setError(null);
            Log.e(TAG, "Address  fields  not nul" + address);
        }

//        if (rv_productquanity.getVisibility() == View.VISIBLE) {
//            for (int i = 0; i < adatper.mlist.size(); i++) {
//                if (adatper.mlist.get(i).getManualQuanty().isEmpty()) {
//                    Log.e(TAG, "manual qauntity is empty " + adatper.mlist.get(i).getManualQuanty());
//                }
//                Log.e(TAG, "manual qauntity show " + adatper.mlist.get(i).getManualQuanty());
//            }
//            Toast.makeText(this, getResources().getString(R.string.qauntity_fileld_error), Toast.LENGTH_LONG).show();
//        }


        return valid;

    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        keepme_str = keepme_str + buttonView.getText().toString();
        Log.e(TAG, "onCheckedChanged" + keepme_str + "icchekd " + isChecked);

    }


    /*go to paypal activity to get status and result*/
    private void getpaymentwithpaypal() {
        //Creating a paypalpayment
        paymentAmount = et_amount_without_gods.getText().toString();
        if (paymentAmount.isEmpty()) {
            Log.e(TAG, "paymentAmount is empty to retrun=> " + paymentAmount);
            return;
        }
        //Creating a paypalpayment
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(paymentAmount)), "USD", "Simplified Coding Fee",
                PayPalPayment.PAYMENT_INTENT_SALE);
        //Creating Paypal Payment activity intent
        Intent intent = new Intent(this, PaymentActivity.class);
        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        //Puting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        //Starting the intent activity for result
        //the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the result is from paypal
        if (requestCode == PAYPAL_REQUEST_CODE) {

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                //if confirmation is not null
                if (confirm != null) {
                    try {
                        //Getting the payment details
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        Log.e(TAG, paymentDetails);
                        JSONObject jsonObject = new JSONObject(paymentDetails);
                        JSONObject jsonresponse = jsonObject.getJSONObject("response");
                        String create_time = jsonresponse.getString("create_time");
                        String id = jsonresponse.getString("id");
                        String state = jsonresponse.getString("state");
                        String bussinesstype = jsonresponse.getString("intent");
                        String response_type = jsonObject.getString("response_type");
                        Log.e(TAG, "jsojParse create_time " + create_time + "\nid " + id + "\nstate " + state + "\nbussinesstype " + bussinesstype + "\nreponseType " + response_type);


                        call_charitydonat_api(create_time, id, state, bussinesstype, response_type);

                        //Starting a new activity for the payment details and also putting the payment details with intent
//                        startActivity(new Intent(this, CharityDonateActivity.class)
//                                .putExtra("PaymentDetails", paymentDetails)
//                                .putExtra("PaymentAmount", paymentAmount));

                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }


//    /*for using paypal implementation*/
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        //If the result is from paypal
////        if (requestCode == PAYPAL_REQUEST_CODE) {
//            //If the result is OK i.e. user has not canceled the payment
//            if (resultCode == Activity.RESULT_OK) {
//                //Getting the payment confirmation
//                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
//                //if confirmation is not null
//                if (confirm != null) {
//                    try {
//                        //Getting the payment details
//                        String paymentDetails = confirm.toJSONObject().toString(4);
//                        Log.i("paymentExample", paymentDetails);
//                        Log.e(TAG, "paymentsDetal response=> " + paymentDetails);
//
////                        //Starting a new activity for the payment details and also putting the payment details with intent
////                        startActivity(new Intent(this, ConfirmationActivity.class)
////                                .putExtra("PaymentDetails", paymentDetails)
////                                .putExtra("PaymentAmount", paymentAmount));
//
//                    } catch (JSONException e) {
//                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
//                    }
//                }
//            } else if (resultCode == Activity.RESULT_CANCELED) {
//                Log.i(TAG, "The user canceled.");
//            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
//                Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
//            }
//        }
//
//    }


//    public void onResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == Activity.RESULT_OK) {
//            if (data.hasExtra(PayPalActivity.EXTRA_PAY_KEY)) {
//                Log.d("FD - pay key", data.getStringExtra(PayPalActivity.EXTRA_PAY_KEY));
//            }
//            if (data.hasExtra(PayPalActivity.EXTRA_PAYMENT_STATUS)) {
//                Log.d("FD - pay status", data.getStringExtra(PayPalActivity.EXTRA_PAYMENT_STATUS));
//            }
//
//            try {
//                Log.e(TAG, "Response " + data.getStringExtra(PayPalActivity.EXTRA_PAY_KEY));
//
//            } catch (Exception e) {
//                Dialog.simpleWarning(R.string.paypal_failed, this);
//                Log.d("paymentExample", "an extremely unlikely failure occurred: ", e);
//            }
//        } else if (resultCode == Activity.RESULT_CANCELED) {
//            // user canceled payment
//        } else if (resultCode == PayPalActivity.RESULT_FAILURE) {
//            Dialog.simpleWarning(data.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE), this);
//        }
//    }



  /*  {
        "client": {
        "environment": "sandbox",
                "paypal_sdk_version": "2.6.0",
                "platform": "Android",
                "product_name": "PayPal-Android-SDK"
    },
        "response": {
        "create_time": "2019-08-13T10:57:42Z",
                "id": "PAYID-LVJJPJQ5RM09644FS366723X",
                "intent": "sale",
                "state": "approved"
    },
        "response_type": "payment"
    }*/


}
