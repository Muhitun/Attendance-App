package com.app.attendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.attendance.Adapter.JobClicked;
import com.app.attendance.Adapter.StoreListAdapter;
import com.app.attendance.Api.APIInterface;
import com.app.attendance.Api.ApiUtils;
import com.app.attendance.Model.StoreListResponse;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "attendance_"+this.getClass().getSimpleName();
    StoreListAdapter storeListAdapter;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    ProgressDialog dialog;
    private CompositeDisposable mCompositeDisposable;
    APIInterface apiInterface;
    List<StoreListResponse.Data> storeList;
    Boolean isScrolling = false;
    String more = null;
    int currentPage;
    int currentItems, totalItems, scolledOutItems;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUI();
    }

    public void setUI(){
        mCompositeDisposable = new CompositeDisposable();
        apiInterface = ApiUtils.getService();
        storeList = new ArrayList<>();
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.rvStoreList);
        currentPage = 1;
        storeListAdapter = new StoreListAdapter(getApplicationContext(), storeList, new JobClicked() {
            @Override
            public void jobClicked(int position) {
                Intent i = new Intent(MainActivity.this, SubmitAttendanceActivity.class);
                i.putExtra("name",storeList.get(position).getName());
                startActivity(i);
            }
        });

        layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.hasFixedSize();
        recyclerView.setAdapter(storeListAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems = layoutManager.getChildCount();
                totalItems = layoutManager.getItemCount();
                scolledOutItems = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (isScrolling && (currentItems + scolledOutItems == totalItems)) {

                    isScrolling = false;
                    fetchData();
                }
            }
        });

        getStoreListList();
    }

    public void fetchData(){
        progressBar.setVisibility(View.VISIBLE);
        if(isNetworkAvailable()){
            if(more!=null){
                currentPage++;
                getStoreListList();
            }else{
                progressBar.setVisibility(View.INVISIBLE);
            }

        }else{
            Toast.makeText(getApplicationContext(), "Please check internet connections ", Toast.LENGTH_LONG).show();
        }
    }

    public void getStoreListList(){

        if(isNetworkAvailable()){
            dialog = ProgressDialog.show(MainActivity.this, "", "Data retrieving. Please wait.....", true);
            mCompositeDisposable.add(apiInterface.getStoreList(currentPage) //
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseStoreList, this::handleErrorStoreList));
        }else{
            AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
            ad.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getStoreListList();
                }
            });
            ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            ad.setMessage("Please check your internet connection and try again");
            ad.setTitle("Error");
            ad.setCancelable(false);
            ad.show();
        }
    }


    private void handleResponseStoreList(StoreListResponse storeResponse) {
        dialog.dismiss();
        more = storeResponse.getLinks().getNext();
        storeList.addAll(storeResponse.getData());
        storeListAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), "List retrieved", Toast.LENGTH_SHORT).show();
    }

    private void handleErrorStoreList(Throwable error) {
        dialog.dismiss();
        Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkAvailable(){

        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE); // from arman
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else return false;
    }
}
