package com.modip.vaccinenotifier;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class VaccineByPinFragment extends Fragment implements VaccineByStateAndDistrictFragmentClickListener{
    private Context mContext;
    private Spinner pollingIntervalSpinner;
    private ArrayList<VaccineDataModel> vaccineDataList = new ArrayList<>();
    private Boolean isVaccineDataAvailable = false;
    private TextView tv_lastChecked;
    private RecyclerView recyclerView;
    private VaccineDataAdapter adapter;
    private EditText et_pin;
    private CheckBox cb_18age;
    private CheckBox cb_45age;
    Handler handler;
    Boolean is18Checked = false;
    Boolean is45Checked = false;

    VaccineByPinFragment(Context mContext) {
        this.mContext = mContext;
    }

    public VaccineByPinFragment(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layout_id = R.layout.vaccine_by_pin_fragment;
        View view = inflater.inflate(layout_id, container, false);

        AdView mAdView = view.findViewById(R.id.pin_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        et_pin = view.findViewById(R.id.enter_pin);
        tv_lastChecked = view.findViewById(R.id.tv_pin_last_checked);
        cb_18age = view.findViewById(R.id.pin_checkbox_18);
        cb_45age = view.findViewById(R.id.pin_checkbox_45);
        pollingIntervalSpinner = view.findViewById(R.id.pin_select_polling_interval_spinner);
        recyclerView = view.findViewById(R.id.pin_vaccinedata_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setNestedScrollingEnabled(false);
        setPollingIntervalSpinner();

        final Button btn_startMonitoring = view.findViewById(R.id.pin_start_monitoring_btn);
        final Button btn_stopMonitoring = view.findViewById(R.id.pin_stop_monitoring_btn);
        btn_startMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String pincode = et_pin.getText().toString();
                if(pincode.isEmpty()){
                    et_pin.setError(getString(R.string.enter_valid_pincode));
                }
                else if(pincode.length() != 6){
                    et_pin.setError(getString(R.string.enter_valid_pincode));
                }
                else{
                    et_pin.setEnabled(false);
                    btn_stopMonitoring.setEnabled(true);
                    cb_18age.setEnabled(false);
                    cb_45age.setEnabled(false);
                    btn_startMonitoring.setEnabled(false);
                    pollingIntervalSpinner.setEnabled(false);
                    is18Checked = cb_18age.isChecked();
                    is45Checked = cb_45age.isChecked();
                    final StateModel pollingSpinnerModel = (StateModel) pollingIntervalSpinner.getSelectedItem();
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new getVaccineData().execute(pincode);
                            handler.postDelayed(this, pollingSpinnerModel.getId());
                        }
                    }, 20000);
                }
            }
        });

        btn_stopMonitoring.setEnabled(false);
        btn_stopMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_pin.setEnabled(true);
                btn_startMonitoring.setEnabled(true);
                btn_stopMonitoring.setEnabled(false);
                cb_18age.setEnabled(true);
                cb_45age.setEnabled(true);
                pollingIntervalSpinner.setEnabled(true);
                handler.removeCallbacksAndMessages(null);
            }
        });

        return view;
    }

    @Override
    public void itemClicked(View view, int position) {
    }

    private void setPollingIntervalSpinner(){
        ArrayList<StateModel> pollingInvervalList = new ArrayList<>();
        pollingInvervalList.add(new StateModel(15000, "15 seconds"));
        pollingInvervalList.add(new StateModel(30000, "30 seconds"));
        pollingInvervalList.add(new StateModel(45000, "45 seconds"));
        pollingInvervalList.add(new StateModel(60000, "1 minute"));
        pollingInvervalList.add(new StateModel(120000, "2 minute"));
        pollingInvervalList.add(new StateModel(600000, "10 minute"));
        pollingInvervalList.add(new StateModel(1800000, "30 minute"));
        pollingInvervalList.add(new StateModel(3600000, "1 hour"));
        ArrayAdapter<StateModel> spinnerAdapter = new ArrayAdapter<StateModel>(mContext, android.R.layout.simple_spinner_item,pollingInvervalList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pollingIntervalSpinner.setAdapter(spinnerAdapter);
        spinnerAdapter.notifyDataSetChanged();
    }

    public class getVaccineData extends AsyncTask<String,String,String> {
        AlertDialog progressDialog;
        Boolean isCallSuccess = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isVaccineDataAvailable = false;
            progressDialog = new SpotsDialog.Builder()
                    .setContext(getContext())
                    .setTheme(R.style.Custom)
                    .setMessage(R.string.app_name)
                    .build();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... values) {
            try {
                String pincode = values[0];
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                String urlStr = getString(R.string.api_base_url)+ "appointment/sessions/public/calendarByPin?pincode="+pincode+"&date="+currentDate;
                Log.d("json",urlStr);
                URL url = new URL(urlStr);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
                InputStream inputStream = url.openStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String result="";
                String line="";
                while((line = bufferedReader.readLine())!= null) {
                    result += line;
                }
                String finalJson = result.toString();
                if(!finalJson.isEmpty()) {
                    JSONObject parentJson = new JSONObject(finalJson);
                    vaccineDataList.clear();
                    JSONArray dataArray = parentJson.getJSONArray("centers");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject tempObj = dataArray.getJSONObject(i);
                        JSONArray sessionsArray = tempObj.getJSONArray("sessions");
                        for(int j=0; j < sessionsArray.length(); j++){
                            JSONObject tempSessionObj = sessionsArray.getJSONObject(j);
                            int capacity = tempSessionObj.getInt("available_capacity");
                            int dose1Capacity = tempSessionObj.getInt("available_capacity_dose1");
                            int dose2Capacity = tempSessionObj.getInt("available_capacity_dose2");
                            if(capacity > 0){
                                isVaccineDataAvailable = true;
                                int minAgeLimit = tempSessionObj.getInt("min_age_limit");
                                if(is18Checked && is45Checked){
                                    vaccineDataList.add(new VaccineDataModel(tempObj.getInt("center_id"), tempObj.getString("address"), tempObj.getString("name"), capacity, dose1Capacity, dose2Capacity, tempSessionObj.getInt("min_age_limit"), tempSessionObj.getString("vaccine"), tempObj.getInt("pincode"), tempSessionObj.getString("date")));
                                }
                                else if(is18Checked && minAgeLimit == 18){
                                    vaccineDataList.add(new VaccineDataModel(tempObj.getInt("center_id"), tempObj.getString("address"), tempObj.getString("name"), capacity, dose1Capacity, dose2Capacity, tempSessionObj.getInt("min_age_limit"), tempSessionObj.getString("vaccine"), tempObj.getInt("pincode"), tempSessionObj.getString("date")));
                                }
                                else if(is45Checked && minAgeLimit == 45){
                                    vaccineDataList.add(new VaccineDataModel(tempObj.getInt("center_id"), tempObj.getString("address"), tempObj.getString("name"), capacity, dose1Capacity, dose2Capacity, tempSessionObj.getInt("min_age_limit"), tempSessionObj.getString("vaccine"), tempObj.getInt("pincode"), tempSessionObj.getString("date")));
                                }
                            }
                        }
                    }
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                isCallSuccess = true;
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(progressDialog.isShowing()){
                progressDialog.hide();
            }
            if(isCallSuccess){
                DateFormat df = new SimpleDateFormat("dd-MMMM-YYYY hh:mm:ss a");
                String date = df.format(Calendar.getInstance().getTime());
                tv_lastChecked.setText(getString(R.string.last_checked_at) + " "+ date);
                //if(isVaccineDataAvailable){
                    setupRecyclerViewforVaccineData();
                //}
            }
            else{
                if(!isNetworkConnected()){
                    Toast.makeText(getContext(), getString(R.string.network_unavailable), Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getContext(), getString(R.string.api_error_general_msg), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void setupRecyclerViewforVaccineData(){
        //if(!vaccineDataList.isEmpty()){
            adapter = new VaccineDataAdapter(vaccineDataList, mContext);
            recyclerView.setAdapter(adapter);
        //}
        if(!vaccineDataList.isEmpty())
            notifyUser();
    }

    private void notifyUser(){
        NotificationCompat.Builder builder;
        NotificationManager notificationManager;
        NotificationChannel channel;
        CharSequence name = getString(R.string.notificationchannelname);
        String description = getString(R.string.notificationchanneldesc);
        int importance = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }
        channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(getString(R.string.notificationchannelid), name, importance);
            channel.setDescription(description);
            channel.setVibrationPattern(new long[] { 1000, 1000, 1000, 1000, 1000 });
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
        }
        notificationManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = mContext.getSystemService(NotificationManager.class);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        if(!vaccineDataList.isEmpty()){
            builder = new NotificationCompat.Builder(mContext, getString(R.string.notificationchannelid));
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class), 0);
            builder.setContentTitle("Slots Are Available")
                    .setContentText("Check App for more details")
                    .setSmallIcon(R.drawable.logo)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent);
            if (notificationManager != null) {
                notificationManager.notify(1, builder.build());
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        }
        else
            return false;
    }
}
