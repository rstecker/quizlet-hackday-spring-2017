package sixarmstudios.quizletcolors;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.client.IClientService;
import com.example.bluetooth.client.PlayerService;
import com.example.bluetooth.core.IBluetoothHostListener;
import com.example.bluetooth.core.IBluetoothPlayerListener;
import com.example.bluetooth.server.HostService;
import com.example.bluetooth.server.IServerService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class StartActivity extends Activity implements IBluetoothHostListener, IBluetoothPlayerListener {
    public static final int REQUEST_ENABLE_BT = 10;
    public static final int REQUEST_PERMISSIONS_CODE = 11;
    public static final int REQUEST_DISCOVERABLE_CODE = 12;
    @LayoutRes
    public static final int LAYOUT_ID = R.layout.activity_start;
    public static final String TAG = StartActivity.class.getSimpleName();

    @BindView(R.id.username_text_field) EditText mUsernameField;
    @BindView(R.id.hosting_debug) TextView mDebugHost;
    @BindView(R.id.joining_debug) TextView mDebugJoin;
    @BindView(R.id.start_hosting) CheckedTextView mHostButton;
    @BindView(R.id.join_option_list) LinearLayout mJoinList;

    boolean mModelBound = false;
    IServerService mServerService;
    boolean mServerBound = false;
    IClientService mClientService;
    boolean mClientBound = false;

    // region Lifecycle stuff
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);
        ButterKnife.bind(this);
        mJoinList.removeAllViews();
    }


    List<String> mockUsernames = Arrays.asList("Bob", "Joe", "Sam", "Kelly", "Jude", "Karen",
            "Rebecca", "Miguel", "Amy", "Matt", "Amanda", "Damien", "Ankush", "Lindsey", "Lisa",
            "Suko", "Trisha", "Kathleen", "Ilkay", "Brenda");

    @Override
    protected void onStart() {
        super.onStart();
        mUsernameField.setText(mockUsernames.get((int) (Math.random() * ((mockUsernames.size() - 1) + 1))));
        // if I only JUST bind, the service dies when we background :'(
        // TODO : inspect these flags, I bet we want a different ont
//        bindService(new Intent(this, ModelRetrievalService.class), mModelConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, HostService.class), mServerConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, PlayerService.class), mClientConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
//        if (mModelBound) {
//            unbindService(mModelConnection);
//            mModelBound = false;
//        }
        if (mServerBound) {
            unbindService(mServerConnection);
            mServerBound = false;
        }
        if (mClientBound) {
            unbindService(mClientConnection);
            mClientBound = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, R.string.bluetooth_denied, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Bluetooth request denied");
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, R.string.bluetooth_approved, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Bluetooth enabled");
        } else if (requestCode == REQUEST_DISCOVERABLE_CODE) {
            if (resultCode <= 0) {
                Log.e(TAG, "Denied discoverability");
                Toast.makeText(this, R.string.discoverability_denied, Toast.LENGTH_LONG).show();
            } else {
                Log.i(TAG, "Discoverable mode enabled for " + resultCode + " seconds");
                startHostDiscoverabilityWindow(resultCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Permission denied");
                            return;
                        }
                    }
                    Toast.makeText(this, R.string.permission_approved, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Permission granted");
                }
                break;
            default:
        }
    }
    //endregion

    // region TEMP UI stuff (should be spun out somewhere else later)

    @OnClick(R.id.start_hosting)
    public void handleStartHostingClick() {
        if (mServerBound) {
            mDebugHost.setText("Hosting as : " + mServerService.startHosting(this) + "\n" + mDebugHost.getText());
        } else {
            Log.e(TAG, "Wanted to be a Host but we're not server bound yet");
        }
    }

    @OnClick(R.id.join_game)
    public void handleJoinGameClick() {
        if (mClientBound) {
            mClientService.startLooking(this);
        } else {
            Log.e(TAG, "Wanted to be a Player but we're not server bound yet");
        }
    }

    private void startHostDiscoverabilityWindow(int seconds) {
        String toastMsg = String.format(getResources().getString(R.string.host_discoverable_toast), seconds);
        Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
        mHostButton.setChecked(true);
        mHostButton.setText(R.string.host_button_discoverable);
        Single.timer(seconds, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((t) -> { // do I need to worry about the button no longer being around?
                    mHostButton.setText(R.string.host_button);
                    mHostButton.setChecked(false);
                    Log.i(TAG, "Host no longer discoverable after " + seconds + " seconds");
                });
    }

    private void addToGameOptionList(@NonNull BluetoothDevice device, @Nullable String name, int bondState, @NonNull String address) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView v = new TextView(this, null, 0, R.style.GameOption);

        StringBuilder sb = new StringBuilder();
        sb.append(name == null ? "unknown" : name);
        sb.append(" ");
        sb.append(bondState == BluetoothDevice.BOND_BONDING ? "[bonding] " : bondState == BluetoothDevice.BOND_BONDED ? "[bonded] " : "");
        sb.append(address);
        v.setText(sb.toString());
        v.setLayoutParams(layoutParams);
        if (bondState == BluetoothDevice.BOND_BONDED) {
            mJoinList.addView(v, 0);
        } else {
            mJoinList.addView(v);
        }
        v.setOnClickListener((view) -> {
            if (mClientBound) {
                mClientService.connectToServer(device);
            }
        });
    }

    //endregion

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mServerConnection = new ServiceConnection() {

        int msgCount = 0;

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            HostService.LocalBinder binder = (HostService.LocalBinder) service;
            mServerService = binder.getService();
            mServerService.getMessageUpdates()
                    .take(1)
                    .flatMap((s) -> Flowable.interval(2, TimeUnit.SECONDS))
                    .subscribe(
                            (t) -> {
                                mServerService.sendMsg("Tick tock, I've seen " + msgCount);
                            },
                            (e) -> Log.e(TAG, "Ping back error " + e));

            mServerService.getMessageUpdates()
                    .scan(
                            (s1, s2) -> {
                                ++msgCount;
                                String newString = "[" + msgCount + "] " + s2 + "\n" + s1;
                                return newString.substring(0, Math.min(300, newString.length()));
                            })
                    .subscribe(
                            (text) -> {
                                mDebugHost.setText(text);
                            },
                            (e) -> {
                                Log.e(TAG, "Received error from server service : " + e);
                            },
                            () -> {
                                mDebugHost.setText("Connection shut down : " + msgCount);
                            }
                    );
            mServerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mServerBound = false;
        }
    };

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mClientConnection = new ServiceConnection() {

        int msgCount = 0;

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mClientService = binder.getService();
            mClientService.getMessageUpdates().subscribe(
                    (msg) -> {
                        mDebugJoin.setText(++msgCount + "] " + msg);
                        mClientService.sendMsg("Mama, I heard you : " + msgCount);
                    },
                    (e) -> {
                        Log.e(TAG, "Received error from client service : " + e);
                    },
                    () -> {
                        mDebugJoin.setText("Connection shut down : " + msgCount);
                    });
            mClientBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mClientBound = false;
        }
    };

    //region IBluetoothClientListener implementation

    @Override
    public void onDeviceFound(@NonNull BluetoothDevice device, @Nullable String name, int bondState, @NonNull String address) {
        Log.e(TAG, "onDeviceFound  : " + name + " : " + bondState + " : " + address);
        addToGameOptionList(device, name, bondState, address);
    }

    @Override
    public void requestDiscoverabilityIntent(@NonNull Intent intent) {
        Log.i(TAG, "Starting Discoverability Intent");
        startActivityForResult(intent, REQUEST_DISCOVERABLE_CODE);
    }

    @Override
    public void isDiscoverable(boolean isDiscoverable) {
        Log.e(TAG, "isDiscoverable  : " + isDiscoverable);
    }

    @Override
    public void requestPermission(@NonNull String[] requestedPermissions) {
        requestPermissions(requestedPermissions, REQUEST_PERMISSIONS_CODE);
    }

    @Override
    public void requestBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    //endregion
}
