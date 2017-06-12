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
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
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
import sixarmstudios.quizletcolors.ui.board.BoardFragment;
import sixarmstudios.quizletcolors.ui.lobby.LobbyFragment;
import ui.BoardViewModel;
import ui.Fact;
import ui.Game;
import ui.LobbyViewModel;

import static sixarmstudios.quizletcolors.logic.SetupHelper.MOCK_USERNAMES;


public class StartActivity extends LifecycleActivity implements IBluetoothHostListener, IBluetoothPlayerListener {
    @LayoutRes public static final int LAYOUT_ID = R.layout.activity_start;
    public static final String TAG = StartActivity.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 10;
    public static final int REQUEST_PERMISSIONS_CODE = 11;
    public static final int REQUEST_DISCOVERABLE_CODE = 12;

    private static final String PLAYER_STATE_KEY = "player_state";

    @BindView(R.id.username_text_field) EditText mUsernameField;
    @BindView(R.id.start_hosting) CheckedTextView mHostButton;
    @BindView(R.id.join_game) CheckedTextView mJoinButton;
    @BindView(R.id.join_option_list) LinearLayout mJoinList;

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

    @Override
    protected void onStart() {
        super.onStart();
        // if I only JUST bind, the service dies when we background :'(
        // TODO : inspect these flags, I bet we want a different ont
        if (mPlayerState == PlayerState.HOST || mPlayerState == PlayerState.UNKNOWN) {
            bindService(new Intent(this, HostService.class), mHostConnection, Context.BIND_AUTO_CREATE);
        }
        if (mPlayerState == PlayerState.PLAYER || mPlayerState == PlayerState.UNKNOWN) {
            bindService(new Intent(this, PlayerService.class), mPlayerConnection, Context.BIND_AUTO_CREATE);
        }

        LobbyViewModel viewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
        if (mPlayerState == PlayerState.UNKNOWN) {
            mUsernameField.setText(MOCK_USERNAMES.get((int) (Math.random() * ((MOCK_USERNAMES.size() - 1) + 1))));
            viewModel.resetGame();
        }
        viewModel.getFacts().observe(this, this::handleContentUpdates);
        viewModel.getGame().observe(this, this::handleGameUpdates);
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

    @OnClick(R.id.start_hosting)
    public void handleStartHostingClick() {
        mPlayerState = PlayerState.HOST;
        mJoinButton.setVisibility(View.GONE);
        mUsernameField.setVisibility(View.GONE);
        if (mPlayerConnection.isBound()) {
            mPlayerConnection.unbindService(this);
        }
        if (mHostConnection.isBound()) {
            LobbyViewModel viewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
            viewModel.resetGame();
            String hostName = mHostConnection.startHosting(this, mUsernameField.getText().toString());
            if (StringUtils.isEmpty(hostName)) {
                Toast.makeText(this, R.string.no_host_name, Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.setUpNewGame(hostName);
            initHostConnectionObservables();
        } else {
            Toast.makeText(this, R.string.not_connected_yet_try_again, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Wanted to be a Host but we're not server bound yet");
        }
    }

    @OnClick(R.id.join_game)
    public void handleJoinGameClick() {
        mPlayerState = PlayerState.PLAYER;
        mHostButton.setVisibility(View.GONE);
        mUsernameField.setVisibility(View.GONE);
        if (mHostConnection.isBound()) {
            mHostConnection.unbindService(this);
        }
        if (mPlayerConnection.isBound()) {
            LobbyViewModel model = ViewModelProviders.of(this).get(LobbyViewModel.class);
            model.resetGame();
            mPlayerConnection.startLooking(this);
        } else {
            Toast.makeText(this, R.string.not_connected_yet_try_again, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Wanted to be a Player but we're not server bound yet");
        }
    }


    private void handleContentUpdates(List<Fact> facts) {
        mHostConnection.setContent(facts);
    }

    private void handleGameUpdates(List<Game> games) {
        if (games == null || games.size() != 1) {
            clearFragments();
            return;
        }
        Game game = games.get(0);
        switch (game.getState()) {
            case WAITING:
                ensureLobbyFragmentUp();
                return;
            case CAN_START:
                ensureLobbyFragmentUp();
                return;
            case START:
                ensureBoardFragmentUp();
                mHostConnection.startGame();
                break;
            case PLAYING:
                ensureBoardFragmentUp();
                break;
            default:
                throw new IllegalStateException("Unable to handle game state : " + game.getState());
        }
    }

    private void clearFragments() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                    .commit();
        }
    }

    private void ensureBoardFragmentUp() {
        if (getSupportFragmentManager().findFragmentByTag(BoardFragment.TAG) == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, BoardFragment.newInstance());
            fragmentTransaction.commit();
        }
    }

    private void ensureLobbyFragmentUp() {
        if (getSupportFragmentManager().findFragmentByTag(LobbyFragment.TAG) == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, LobbyFragment.newInstance());
            fragmentTransaction.commit();
        }
    }

    private void initPlayerConnectionObservables() {
        mPlayerConnection.getBoardStateUpdates().observeOn(Schedulers.io()).subscribe(
                (state) -> {
                    BoardViewModel boardViewModel = ViewModelProviders.of(this).get(BoardViewModel.class);
                    boardViewModel.processGameUpdate(state);
                },
                (e) -> Log.e(TAG, "Error updating board view model [" + Thread.currentThread().getName() + "] " + e)
        );
        mPlayerConnection.getLobbyStateUpdates().subscribe(
                (msg) -> {
                    LobbyViewModel lobbyViewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
                    lobbyViewModel.processLobbyUpdate(msg);
                },
                (e) -> {
                    Log.e(TAG, "Received error from Player service : " + e);
                });
    }

    private void initHostConnectionObservables() {
        mHostConnection.getLobbyStateUpdates().observeOn(Schedulers.io()).subscribe(
                (state) -> {
                    LobbyViewModel model = ViewModelProviders.of(this).get(LobbyViewModel.class);
                    model.processLobbyUpdate(state);
                },
                (e) -> Log.e(TAG, "Error updating lobby view model [" + Thread.currentThread().getName() + "] " + e)
        );
        mHostConnection.getBoardStateUpdates().observeOn(Schedulers.io()).subscribe(
                (state) -> {
                    BoardViewModel viewModel = ViewModelProviders.of(this).get(BoardViewModel.class);
                    viewModel.processGameUpdate(state);
                    ensureBoardFragmentUp();
                },
                (e) -> Log.e(TAG, "Error updating board view model [" + Thread.currentThread().getName() + "] " + e)
        );
        mHostConnection.getStartStatusUpdates().observeOn(Schedulers.io()).subscribe(
                (canStart) -> {
                    LobbyViewModel viewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
                    viewModel.setGameState(canStart ? Game.State.CAN_START : Game.State.WAITING);
                },
                (e) -> Log.e(TAG, "Error updating start state [" + Thread.currentThread().getName() + "] " + e)
        );
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
                initPlayerConnectionObservables();
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
