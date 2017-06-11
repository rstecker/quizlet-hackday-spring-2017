package sixarmstudios.quizletcolors;

import android.app.Activity;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.client.PlayerService;
import com.example.bluetooth.core.IBluetoothHostListener;
import com.example.bluetooth.core.IBluetoothPlayerListener;
import com.example.bluetooth.server.HostService;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sixarmstudios.quizletcolors.connections.HostServiceConnection;
import sixarmstudios.quizletcolors.connections.PlayerServiceConnection;
import sixarmstudios.quizletcolors.ui.LobbyFragment;
import sixarmstudios.quizletcolors.ui.LobbyViewModel;
import ui.Game;


public class StartActivity extends LifecycleActivity implements IBluetoothHostListener, IBluetoothPlayerListener {
    @LayoutRes public static final int LAYOUT_ID = R.layout.activity_start;
    public static final String TAG = StartActivity.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 10;
    public static final int REQUEST_PERMISSIONS_CODE = 11;
    public static final int REQUEST_DISCOVERABLE_CODE = 12;

    private static final String PLAYER_STATE_KEY = "player_state";

    @BindView(R.id.username_text_field) EditText mUsernameField;
    @BindView(R.id.hosting_debug) TextView mDebugHost;
    @BindView(R.id.joining_debug) TextView mDebugJoin;
    @BindView(R.id.start_hosting) CheckedTextView mHostButton;
    @BindView(R.id.join_option_list) LinearLayout mJoinList;


    public enum PlayerState {
        UNKNOWN,
        HOST,
        PLAYER
    }

    private PlayerState mPlayerState = PlayerState.UNKNOWN;
    private HostServiceConnection mHostConnection = new HostServiceConnection(this);
    private PlayerServiceConnection mPlayerConnection = new PlayerServiceConnection(this);

    // region Lifecycle stuff
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);
        ButterKnife.bind(this);
        mJoinList.removeAllViews();
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPlayerState = (PlayerState) savedInstanceState.get(PLAYER_STATE_KEY);
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PLAYER_STATE_KEY, mPlayerState);
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
        if (mPlayerState == PlayerState.HOST || mPlayerState == PlayerState.UNKNOWN) {
            bindService(new Intent(this, HostService.class), mHostConnection, Context.BIND_AUTO_CREATE);
        }
        if (mPlayerState == PlayerState.PLAYER || mPlayerState == PlayerState.UNKNOWN) {
            bindService(new Intent(this, PlayerService.class), mPlayerConnection, Context.BIND_AUTO_CREATE);
        }

        if (getSupportFragmentManager().findFragmentByTag(LobbyFragment.TAG) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.lobby_users_fragment_container, LobbyFragment.newInstance(), LobbyFragment.TAG)
                    .commit();
//            FragmentManager fm = getSupportFragmentManager();
//
//            FragmentTransaction ft = fm.beginTransaction();
//            LobbyFragment lobbyFragment = new LobbyFragment();
//            ft.replace(R.id.listFragment, (Fragment) lobbyFragment);
//            ft.commit();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mHostConnection.isBound()) {
            mHostConnection.unbindService(this);
        }
        if (mPlayerConnection.isBound()) {
            mPlayerConnection.unbindService(this);
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
        mPlayerState = PlayerState.HOST;
        LobbyViewModel model = ViewModelProviders.of(this).get(LobbyViewModel.class);
        model.resetGame();
        if (mPlayerConnection.isBound()) {
            mPlayerConnection.unbindService(this);
        }
        if (mHostConnection.isBound()) {
            String hostName = mHostConnection.startHosting(this, mUsernameField.getText().toString());
            if (StringUtils.isEmpty(hostName)) {
                Toast.makeText(this, R.string.no_host_name, Toast.LENGTH_SHORT).show();
                return;
            }
            model.setUpNewGame(hostName);
            initHostObservables();
            mDebugHost.setText("You are hosting at " + hostName);
            mHostConnection.getLobbyStateUpdates().subscribe(
                            (msg) -> {
                                mDebugJoin.setText(Math.random() + "\nLobby State update. There are " + msg.players().size() + " players.\nYou are the host.");
                            },
                            (e) -> {
                                Log.e(TAG, "Received error from Player service : " + e);
                            },
                            () -> {
                                mDebugJoin.setText("Connection ended");
                            }
                    );
        } else {
            Log.e(TAG, "Wanted to be a Host but we're not server bound yet");
        }
    }

    private void initHostObservables() {
        mHostConnection.getLobbyStateUpdates().observeOn(Schedulers.io()).subscribe(
                (state) -> {
                    LobbyViewModel model = ViewModelProviders.of(this).get(LobbyViewModel.class);
                    if (model != null) {
                        Log.i(TAG, "I'm going in : " + Thread.currentThread().getName());
                        model.processLobbyUpdate(state);
                    } else {
                        Log.w(TAG, "I tried to look up the player vm but it was null");
                    }
                },
                (e) -> Log.e(TAG, "Error updating view model [" + Thread.currentThread().getName() + "] " + e)
        );
        mHostConnection.getStartStatusUpdates().observeOn(Schedulers.io()).subscribe(
                (canStart) -> {
                    LobbyViewModel viewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
                    viewModel.setGameState(canStart ? Game.State.CAN_START : Game.State.WAITING);
                },
                (e) -> Log.e(TAG, "Error updating start state [" + Thread.currentThread().getName() + "] " + e)
        );
    }
    @OnClick(R.id.join_game)
    public void handleJoinGameClick() {
        mPlayerState = PlayerState.PLAYER;
        LobbyViewModel model = ViewModelProviders.of(this).get(LobbyViewModel.class);
        model.resetGame();
        if (mHostConnection.isBound()) {
            mHostConnection.unbindService(this);
        }
        if (mPlayerConnection.isBound()) {
            mPlayerConnection.startLooking(this);
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
            if (mPlayerConnection.isBound()) {
                mJoinList.removeAllViews();
                mPlayerConnection.connectToServer(device, mUsernameField.getText().toString());

                LobbyViewModel viewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
                viewModel.joinNewGame(name, bondState, address);
                mPlayerConnection.getLobbyStateUpdates()
                        .map((msg) -> {
                            LobbyViewModel model = ViewModelProviders.of(this).get(LobbyViewModel.class);
                            model.processLobbyUpdate(msg);
                            return msg;
                        })
                        .subscribe(
                                (msg) -> {
                                    mDebugJoin.setText(Math.random() + "\nLobby State update. There are " + msg.players().size() + " players.");
                                },
                                (e) -> {
                                    Log.e(TAG, "Received error from Player service : " + e);
                                },
                                () -> {
                                    mDebugJoin.setText("Connection ended");
                                }
                        );
            }
        });
    }


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
