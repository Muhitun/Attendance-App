package com.app.attendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.attendance.Api.APIInterface;
import com.app.attendance.Api.ApiUtils;

import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class SubmitAttendanceActivity extends AppCompatActivity {
    private final String TAG = "attendance_"+this.getClass().getSimpleName();
    private static final int REQUEST_LOCATION = 1;
    TextView name;
    EditText empName, empId;
    Button submit;
    ProgressDialog dialog;
    private CompositeDisposable mCompositeDisposable;
    APIInterface apiInterface;
    LocationManager locationManager;
    Double latitude, longitude;
    String alphanumericValue = "";
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_attendance);
        setUI();
    }

    public void setUI(){
        if (ContextCompat.checkSelfPermission(SubmitAttendanceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SubmitAttendanceActivity.this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },100);
        }
        mCompositeDisposable = new CompositeDisposable();
        apiInterface = ApiUtils.getService();

        name = (TextView)findViewById(R.id.tvName);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("name");
            name.setText(value);
        }
        submit = (Button)findViewById(R.id.btnSubmit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()==null){
                    locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        enableGPS();
                    } else {
                        getLocation();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), validate(), Toast.LENGTH_LONG).show();
                }
            }
        });

        empName = (EditText)findViewById(R.id.etName);
        empId = (EditText)findViewById(R.id.etId);
    }
    private String validate() {
        String isValid = null;

        if (TextUtils.isEmpty(empName.getText().toString().trim())) {
            isValid = "Please enter name";
            return isValid;
        }
        if (TextUtils.isEmpty(empId.getText().toString().trim())) {
            isValid = "Please enter id";
            return isValid;
        }
        return isValid;
    }

    public void submitData(){
        if(isNetworkAvailable()){
            dialog = ProgressDialog.show(SubmitAttendanceActivity.this, "", "Attendance posting. Please wait.....", true);
            mCompositeDisposable.add(apiInterface.submitData(empName.getText().toString(), empId.getText().toString(), latitude, longitude, alphanumericValue) //
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseSubmit, this::handleErrorSubmit));
        }else{
            Toast.makeText(getApplicationContext(), "Please check your internet connection and try again", Toast.LENGTH_LONG).show();
        }
    }


    private void handleResponseSubmit(Response<ResponseBody> responseBody) {
        dialog.dismiss();
        AlertDialog.Builder ad = new AlertDialog.Builder(SubmitAttendanceActivity.this);
        ad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        ad.setMessage("Attendance submitted");
        ad.setTitle("Success");
        ad.setCancelable(false);
        ad.show();
    }

    private void handleErrorSubmit(Throwable error) {
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

    private void enableGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                SubmitAttendanceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                SubmitAttendanceActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "permanently denied, please enable location permission from settings", Toast.LENGTH_SHORT).show();
            }
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (locationGPS != null) {
                 latitude = locationGPS.getLatitude();
                 longitude = locationGPS.getLongitude();
                generateString(8);
            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void generateString(int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        alphanumericValue = builder.toString();
        submitData();
    }
}
