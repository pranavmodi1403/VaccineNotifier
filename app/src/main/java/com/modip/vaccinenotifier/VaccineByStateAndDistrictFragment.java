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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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

public class VaccineByStateAndDistrictFragment extends Fragment implements VaccineByStateAndDistrictFragmentClickListener{
    private Spinner stateSpinner;
    private Spinner districtSpinner;
    private Spinner pollingIntervalSpinner;
    private ArrayList<StateModel> stateList = new ArrayList<>();
    private ArrayList<DistrictModel> districtList = new ArrayList<>();
    private ArrayList<VaccineDataModel> vaccineDataList = new ArrayList<>();
    private Boolean isStateDataAvailable = false;
    private Boolean isDistrictDataAvailable = false;
    private Boolean isVaccineDataAvailable = false;
    private Context mContext;
    private TextView tv_lastChecked;
    private CheckBox cb_18age;
    private CheckBox cb_45age;
    private RecyclerView recyclerView;
    private VaccineDataAdapter adapter;
    Handler handler;
    Boolean is18Checked = false;
    Boolean is45Checked = false;

    VaccineByStateAndDistrictFragment(Context mContext) {
        this.mContext = mContext;
    }

    public VaccineByStateAndDistrictFragment(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layout_id = R.layout.vaccine_by_state_and_district_fragment;
        View view = inflater.inflate(layout_id, container, false);

        AdView mAdView = view.findViewById(R.id.district_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        stateSpinner = view.findViewById(R.id.select_state_spinner);
        districtSpinner = view.findViewById(R.id.select_district_spinner);
        pollingIntervalSpinner = view.findViewById(R.id.select_polling_interval_spinner);
        tv_lastChecked = view.findViewById(R.id.tv_last_checked);
        cb_18age = view.findViewById(R.id.checkbox_18);
        cb_45age = view.findViewById(R.id.checkbox_45);
        recyclerView = view.findViewById(R.id.vaccinedata_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setNestedScrollingEnabled(false);
        setPollingIntervalSpinner();
        new getStateList().execute();

        final Button btn_startMonitoring = view.findViewById(R.id.start_monitoring_btn);
        final Button btn_stopMonitoring = view.findViewById(R.id.stop_monitoring_btn);
        btn_startMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateSpinner.setEnabled(false);
                districtSpinner.setEnabled(false);
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
                        DistrictModel districtSpinnerModel = (DistrictModel) districtSpinner.getSelectedItem();
                        if(districtSpinnerModel != null){
                            new getVaccineData().execute(districtSpinnerModel.getId());
                        }
                        handler.postDelayed(this, pollingSpinnerModel.getId());
                    }
                }, 20000);


            }
        });

        btn_stopMonitoring.setEnabled(false);
        btn_stopMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateSpinner.setEnabled(true);
                districtSpinner.setEnabled(true);
                btn_startMonitoring.setEnabled(true);
                btn_stopMonitoring.setEnabled(false);
                pollingIntervalSpinner.setEnabled(true);
                cb_18age.setEnabled(true);
                cb_45age.setEnabled(true);
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

    public class getStateList extends AsyncTask<String,String,String> {
        AlertDialog progressDialog;
        Boolean isCallSuccess = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isStateDataAvailable = false;
            progressDialog = new SpotsDialog.Builder()
                    .setContext(getContext())
                    .setTheme(R.style.Custom)
                    .setMessage(R.string.app_name)
                    .build();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(getString(R.string.api_base_url)+"admin/location/states");
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
                if(!finalJson.isEmpty()){
                    JSONObject parentJson = new JSONObject(finalJson);
                    JSONArray statesArray = parentJson.getJSONArray("states");
                    for(int i=0; i< statesArray.length(); i++){
                        isStateDataAvailable = true;
                        JSONObject tempObj = statesArray.getJSONObject(i);
                        stateList.add(new StateModel(tempObj.getInt("state_id"), tempObj.getString("state_name")));
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
                progressDialog.dismiss();
            }
            if(isCallSuccess && isStateDataAvailable){
                setStateSpinner();
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

    private void setStateSpinner(){
        ArrayAdapter<StateModel> spinnerAdapter = new ArrayAdapter<StateModel>(getContext(), android.R.layout.simple_spinner_item,stateList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(spinnerAdapter);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                StateModel stateSpinnerModel = (StateModel) stateSpinner.getSelectedItem();
                if(stateSpinnerModel != null){
                    new getDistrictData().execute(stateSpinnerModel.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        spinnerAdapter.notifyDataSetChanged();
    }

    public class getDistrictData extends AsyncTask<Integer,String,String> {
        AlertDialog progressDialog;
        Boolean isCallSuccess = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isDistrictDataAvailable = false;
            progressDialog = new SpotsDialog.Builder()
                    .setContext(getContext())
                    .setTheme(R.style.Custom)
                    .setMessage(R.string.app_name)
                    .build();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Integer... strings) {
            try {
                int stateId = strings[0];
                String urlStr = getString(R.string.api_base_url)+ "admin/location/districts/" + stateId;
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
                    districtList.clear();
                    JSONArray dataArray = parentJson.getJSONArray("districts");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject tempObj = dataArray.getJSONObject(i);
                        districtList.add(new DistrictModel(tempObj.getInt("district_id"), tempObj.getString("district_name"), stateId));
                        isDistrictDataAvailable = true;
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
                progressDialog.dismiss();
            }
            if(isCallSuccess && isDistrictDataAvailable){
                setDistrictSpinner(districtList.isEmpty());
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

    private void setDistrictSpinner(Boolean isEmpty){
        if(!isEmpty){
            ArrayAdapter<DistrictModel> spinnerAdapter = new ArrayAdapter<DistrictModel>(getContext(), android.R.layout.simple_spinner_item,districtList);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            districtSpinner.setAdapter(spinnerAdapter);
            spinnerAdapter.notifyDataSetChanged();
        }
    }

    public class getVaccineData extends AsyncTask<Integer,String,String> {
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
        protected String doInBackground(Integer... values) {
            try {
                int districtId = values[0];
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                String urlStr = getString(R.string.api_base_url)+ "appointment/sessions/public/calendarByDistrict?district_id="+districtId+"&date="+currentDate;
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
                progressDialog.dismiss();
            }
            
            if(isCallSuccess){
                DateFormat df = new SimpleDateFormat("dd-MMMM-YYYY hh:mm:ss a");
                String date = df.format(Calendar.getInstance().getTime());
                tv_lastChecked.setText(getString(R.string.last_checked_at) + " "+ date);
//                if(isVaccineDataAvailable){
                    setupRecyclerViewforVaccineData();
//                }
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
//        if(!vaccineDataList.isEmpty()){
            adapter = new VaccineDataAdapter(vaccineDataList, mContext);
            recyclerView.setAdapter(adapter);
//        }
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
                    .setContentText("Check App for more details and Open Co-WIN Portal to book appointment")
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

