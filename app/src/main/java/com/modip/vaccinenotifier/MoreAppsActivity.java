package com.modip.vaccinenotifier;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MoreAppsActivity extends AppCompatActivity {
    ArrayList<moreAppsModel> mList= new ArrayList<moreAppsModel>();
    private RecyclerView recyclerView;
    private MoreAppsAdapter adapter;
    private ShimmerFrameLayout mShimmerViewContainer;
    private Button refreshBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_apps);

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        recyclerView = (RecyclerView) findViewById(R.id.moreapps_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MoreAppsAdapter(mList, MoreAppsActivity.this);
        recyclerView.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.moreapps_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        refreshBtn = findViewById(R.id.moreapps_refresh_btn);
        if(isNetworkConnected()){
            refreshBtn.setVisibility(View.GONE);
            new getData().execute();
        } else{
            recyclerView.setVisibility(View.GONE);
            mShimmerViewContainer.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
            Snackbar snackbar = Snackbar.make(findViewById(R.id.moreAppsContainer), "Internet Connection is Not Available", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkConnected()){
                    mShimmerViewContainer.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    refreshBtn.setVisibility(View.GONE);
                    new getData().execute();
                }
                else{
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.moreAppsContainer), "Internet Connection is Not Available", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

        adapter.setOnItemClickListener(new MoreAppsAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                moreAppsModel currentItem = mList.get(position);
                openApp(currentItem.getApp_playstore_url());
            }
        });
    }

    public class getData extends AsyncTask<Integer, Integer, String> {
        boolean isDataCame = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... integers) {
            try {
                URL url = new URL(getString(R.string.moreapp_url)+"?id=5");
                String header = "Basic " + getString(R.string.moreapp_authcode);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.addRequestProperty("Authorization",header);
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                String finalJson = result.toString();
                if (!finalJson.isEmpty()) {
                    JSONObject parentObject = new JSONObject(finalJson);
                    boolean success = parentObject.getBoolean("success");
                    if(success){
                        JSONArray dataArray = parentObject.getJSONArray("data");
                        mList.clear();
                        for(int i=0; i<dataArray.length(); i++){
                            JSONObject tempObj = dataArray.getJSONObject(i);
                            if(tempObj != null)
                                mList.add(new moreAppsModel(i,tempObj.getString("app_name"),tempObj.getString("app_desc"),tempObj.getString("app_logo_url"),tempObj.getString("app_playstore_url")));
                        }
                    }
                }
                isDataCame = true;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(isDataCame){
                adapter = new MoreAppsAdapter(mList, MoreAppsActivity.this);
                mShimmerViewContainer.stopShimmerAnimation();
                mShimmerViewContainer.setVisibility(View.GONE);
                recyclerView.setAdapter(adapter);
            }
            else{
                if(isNetworkConnected()){
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    refreshBtn.setVisibility(View.GONE);
                }
                else{
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    refreshBtn.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openApp(String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.appNotFOund, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmerAnimation();
    }
    @Override
    protected void onPause() {
        mShimmerViewContainer.stopShimmerAnimation();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        }
        else
            return false;
    }
}
