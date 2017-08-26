package foregroundservice.android.eightleaves.com.foregroundserviceexample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import foregroundservice.android.eightleaves.com.foregroundserviceexample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // A reference to the service used to get location updates.
    private ForegroundService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;
    private MyReceiver myReceiver;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ForegroundService.LocalBinder binder = (ForegroundService.LocalBinder) service;
            mService = binder.getService();
            if(mService.getCount() > 0){
                mBinding.startTimer.setText("Stop");
            }
            mBinding.timer.setText(String.valueOf(mService.getCount()));
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main, null);
        myReceiver = new MyReceiver();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mBinding.startTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBinding.startTimer.getText().equals("Stop")){
                    mService.stopTimer();
                    mBinding.startTimer.setText("Start");
                }else {
                    mService.startTimerService();
                    mBinding.startTimer.setText("Stop");
                }
            }
        });
        bindService(new Intent(this, ForegroundService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(ForegroundService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }

        super.onStop();
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String time = intent.getStringExtra(ForegroundService.EXTRA_TIME);
            if (time != null) {
                mBinding.timer.setText(time);
            }
        }
    }

}
