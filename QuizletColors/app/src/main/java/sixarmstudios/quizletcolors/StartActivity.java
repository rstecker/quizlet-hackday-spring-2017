package sixarmstudios.quizletcolors;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.LongSparseArray;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.bluetooth.client.PlayerService;
import com.example.bluetooth.core.IBluetoothHostListener;
import com.example.bluetooth.core.IBluetoothPlayerListener;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import appstate.AppState;
import appstate.PlayerState;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import quizlet.QSet;
import quizlet.QStudied;
import quizlet.QUser;
import sixarmstudios.quizletcolors.connections.HostServiceConnection;
import sixarmstudios.quizletcolors.connections.PlayerServiceConnection;
import sixarmstudios.quizletcolors.network.IModelRetrievalService;
import sixarmstudios.quizletcolors.network.ModelRetrievalService;
import sixarmstudios.quizletcolors.ui.board.BoardFragment;
import sixarmstudios.quizletcolors.ui.lobby.LobbyFragment;
import sixarmstudios.quizletcolors.ui.setup.LookingForGameFragment;
import sixarmstudios.quizletcolors.ui.setup.LookingForSetFragment;
import sixarmstudios.quizletcolors.ui.setup.StartFragment;
import ui.Fact;
import ui.Game;
import viewmodel.TopLevelViewModel;


public class StartActivity extends LifecycleActivity implements IBluetoothHostListener, IBluetoothPlayerListener {
    @LayoutRes
    public static final int LAYOUT_ID = R.layout.activity_start;
    public static final String TAG = StartActivity.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 10;
    public static final int REQUEST_PERMISSIONS_CODE = 11;
    public static final int REQUEST_DISCOVERABLE_CODE = 12;

    private static final String PLAYER_STATE_KEY = "player_state";

//    @BindView(R.id.username_text_field) EditText mUsernameField;
//    @BindView(R.id.set_text_field) EditText mSetField;
//    @BindView(R.id.start_hosting) CheckedTextView mHostButton;
//    @BindView(R.id.join_game) CheckedTextView mJoinButton;
//    @BindView(R.id.join_option_list) LinearLayout mJoinList;
//    @BindView(R.id.oauth_start) View mOauthButton;

    IModelRetrievalService mModelService;
    boolean mModelBound = false;
    private PlayerState mPlayerState = PlayerState.UNKNOWN_INIT;
    private HostServiceConnection mHostConnection = new HostServiceConnection(this);
    private PlayerServiceConnection mPlayerConnection = new PlayerServiceConnection(this);
    private IBluetoothPlayerListener mPlayerBluetoothListener;

    // region Lifecycle stuff
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);
        ButterKnife.bind(this);

        watchAppState();
//        mJoinList.removeAllViews();
        startService(ModelRetrievalService.startIntent(this, "fakeClientId"));
    }

    private void watchAppState() {
        TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
        viewModel.getAppState().observe(this, appStates -> {
            // Start new applications fresh
            if (appStates == null || appStates.isEmpty()) {
                mPlayerState = PlayerState.UNKNOWN_STARTING;
                viewModel.initApplication(mPlayerState);
                syncFragments();
                return;
            }
            // reset old application state
            AppState appState = appStates.get(0);
            switch (mPlayerState) {
                case UNKNOWN_INIT:
                    mPlayerState = PlayerState.UNKNOWN_STARTING;
                    appState.playState = mPlayerState.toDBVal();
                    viewModel.updateAppState(appState);
                    mBoundModelServiceSubject
                            .filter((bound) -> bound)
                            .take(1)
                            .subscribe((bound) -> {
                                mModelService.restoreQuizletInfo(appState.qToken, appState.qUsername);
                            });
                    break;
                case UNKNOWN_STARTING:
                    PlayerState dbAppState = PlayerState.fromDBVal(appState.playState);
                    switch (dbAppState) {
                        case UNKNOWN_STARTING:
                            break;
                        case ATTEMPT_OAUTH:
                            handleOAuthStart();
                            break;
                        case FIND_GAME:
                            if (mPlayerConnection == null || !mPlayerConnection.isBound()) {
                                Log.i(TAG, "Requesting the Player Service to bind");
                                bindService(new Intent(this, PlayerService.class), mPlayerConnection, Context.BIND_AUTO_CREATE);
                                initPlayerConnectionObservables();
                            }
                            mPlayerState = dbAppState;
                            break;
                        case FIND_SET:
                            mPlayerState = dbAppState;
                            break;
                        default:
                            throw new IllegalStateException("Unknown transition from " + mPlayerState + " -> " + dbAppState);
                    }
                    break;
                default:
                    break;
            }
            syncFragments();
        });

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPlayerState = (PlayerState) savedInstanceState.get(PLAYER_STATE_KEY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PLAYER_STATE_KEY, mPlayerState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // if I only JUST bind, the service dies when we background :'(
        // TODO : inspect these flags, I bet we want a different ont
        bindService(new Intent(this, ModelRetrievalService.class), mModelConnection, Context.BIND_AUTO_CREATE);
//
//        if (mPlayerState == PlayerState.HOST || mPlayerState == PlayerState.UNKNOWN) {
//            bindService(new Intent(this, HostService.class), mHostConnection, Context.BIND_AUTO_CREATE);
//        }
//        if (mPlayerState == PlayerState.PLAYER || mPlayerState == PlayerState.UNKNOWN) {
//            bindService(new Intent(this, PlayerService.class), mPlayerConnection, Context.BIND_AUTO_CREATE);
//        }

//        TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
//        if (mPlayerState == PlayerState.UNKNOWN) {
//            mUsernameField.setText(MOCK_USERNAMES.get((int) (Math.random() * ((MOCK_USERNAMES.size() - 1) + 1))));
//            viewModel.resetGame();
//        }
//        viewModel.getFacts().observe(this, this::handleContentUpdates);
//        viewModel.getGame().observe(this, this::handleGameUpdates);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (mHostConnection.isBound()) {
            mHostConnection.unbindService(this);
            mHostConnection = null;
        }
        if (mPlayerConnection.isBound()) {
            mPlayerConnection.unbindService(this);
            mPlayerConnection = null;
        }
        if (mModelBound) {
            this.unbindService(mModelConnection);
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
//                startHostDiscoverabilityWindow(resultCode);
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


    public void handleOAuthStart() {
        Dialog auth_dialog;
        WebView web;

        auth_dialog = new Dialog(this);
        auth_dialog.setContentView(R.layout.auth_dialog);
        web = auth_dialog.findViewById(R.id.webv);
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(mModelService.getOauthUrl());
        final String redirectUrl = mModelService.getRedirectUrl();

        web.setWebViewClient(new WebViewClient() {
            String authCode;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i(TAG, "We are STARTING " + url);
                if (!url.contains(redirectUrl)) {
                    super.onPageStarted(view, url, favicon);
                    return;
                }
                Uri uri = Uri.parse(url);
                if (url.contains("code=")) {
                    authCode = uri.getQueryParameter("code");
                    Log.i(TAG, "CODE : " + authCode);
                    mModelService.handelOauthCode(StartActivity.this, authCode);
                } else if (url.contains("error=access_denied")) {
                    Log.i(TAG, "ACCESS_DENIED_HERE");
                } else {
                    Log.w(TAG, "OAuth response that doesn't make sense : " + url);
                }
                auth_dialog.dismiss();
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Sharks are fun");
        auth_dialog.setCancelable(true);
    }

//    @OnClick(R.id.start_hosting)
//    public void handleStartHostingClick() {
//        TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
//        mPlayerState = PlayerState.HOST;
//        mJoinButton.setVisibility(View.GONE);
//        mUsernameField.setVisibility(View.GONE);
//        mSetField.setVisibility(View.GONE);
//        if (mPlayerConnection.isBound()) {
//            mPlayerConnection.unbindService(this);
//        }
//
//        if (mHostConnection.isBound()) {
//            String hostName = mHostConnection.startHosting(this, mUsernameField.getText().toString());
//            if (StringUtils.isEmpty(hostName)) {
//                Toast.makeText(this, R.string.no_host_name, Toast.LENGTH_SHORT).show();
//                return;
//            }
//            viewModel.setUpNewGame(hostName);
//            initHostConnectionObservables();
//        } else {
//            Toast.makeText(this, R.string.not_connected_yet_try_again, Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "Wanted to be a Host but we're not server bound yet");
//        }
//    }

//    @OnClick(R.id.join_game)
//    public void handleJoinGameClick() {
//        mPlayerState = PlayerState.PLAYER;
//        mHostButton.setVisibility(View.GONE);
//        mUsernameField.setVisibility(View.GONE);
//        mSetField.setVisibility(View.GONE);
//        if (mHostConnection.isBound()) {
//            mHostConnection.unbindService(this);
//        }
//        if (mPlayerConnection.isBound()) {
//            mPlayerConnection.startLooking(this);
//        } else {
//            Toast.makeText(this, R.string.not_connected_yet_try_again, Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "Wanted to be a Player but we're not server bound yet");
//        }
//    }


//    private void handleContentUpdates(List<Fact> facts) {
//        Log.i(TAG, "Handling content updates : " + facts.size());
//        mHostConnection.setContent(facts);
//        if (facts.size() == 0) {
//            Log.i(TAG, "Can now start game, now that we've cleared facts DB");
//            mHostButton.setVisibility(View.VISIBLE);
//        } else {
//            Log.i(TAG, "Can maybe start game now? Size " + facts + ", button is visible " + (mHostButton.getVisibility() == View.VISIBLE));
//        }
//    }

    private void handleGameUpdates(List<Game> games) {
        if (games == null || games.size() != 1) {
            return;
        }
        Game game = games.get(0);

        if (StringUtils.isNotEmpty(game.selected_option) && StringUtils.isNotEmpty(game.selected_color)) {
            TopLevelViewModel boardViewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
            boardViewModel.moveSubmitted(game.selected_option, game.selected_color);
            if (mPlayerState == PlayerState.PLAYING) {  // TODO : rebecca, gota' fix this
                mPlayerConnection.makeMove(game.selected_option, game.selected_color);
            } else {
                mHostConnection.makeMove(game.selected_option, game.selected_color);
            }
            Log.i(TAG, "Player move submitted : '" + game.selected_option + "' to " + game.selected_color);
        }
    }


    private void syncFragments() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        String currentTag = currentFragment == null ? null : currentFragment.getTag();

        String newTag = null;
        Fragment newFragment = null;
        switch (mPlayerState) {
            case UNKNOWN_INIT:
            case UNKNOWN_STARTING:
            case ATTEMPT_OAUTH:
                if (!StartFragment.TAG.equals(currentTag)) {
                    newTag = StartFragment.TAG;
                    newFragment = StartFragment.newInstance();
                }
                break;
            case FIND_SET:
                if (!LookingForSetFragment.TAG.equals(currentTag)) {
                    newTag = LookingForSetFragment.TAG;
                    newFragment = LookingForSetFragment.newInstance();
                }
                break;
            case FIND_GAME:
                if (!LookingForGameFragment.TAG.equals(currentTag)) {
                    newTag = LookingForGameFragment.TAG;
                    newFragment = LookingForGameFragment.newInstance();
                }
                break;
            case PLAYING:
                if (!BoardFragment.TAG.equals(currentTag)) {
                    newTag = BoardFragment.TAG;
                    newFragment = BoardFragment.newInstance();
                }
                break;
            case LOBBY:
                if (!LobbyFragment.TAG.equals(currentTag)) {
                    newTag = LobbyFragment.TAG;
                    newFragment = LobbyFragment.newInstance();
                }
                break;

            case GAME_OVER:
            default:
                throw new IllegalStateException("Unable to handle state: " + mPlayerState);
        }
        if (newTag != null && newFragment != null) {
            Log.i(TAG, "Transitioning from " + currentTag + " to " + newTag);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment, newTag)
                    .commit();

        }
    }

    private void initPlayerConnectionObservables() {
        mPlayerConnection.getBoardStateUpdates().observeOn(Schedulers.newThread()).subscribe(
                (state) -> {
                    TopLevelViewModel boardViewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
                    boardViewModel.processGameUpdate(state);
                },
                (e) -> Log.e(TAG, "Error updating board view model [" + Thread.currentThread().getName() + "] " + e)
        );
        mPlayerConnection.getLobbyStateUpdates().subscribe(
                (msg) -> {
                    TopLevelViewModel lobbyViewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
                    lobbyViewModel.processLobbyUpdate(msg);
                },
                (e) -> {
                    Log.e(TAG, "Received error from Player service : " + e);
                });
    }

//    private void initHostConnectionObservables() {
//        mHostConnection.getLobbyStateUpdates()
//                .take(1)
//                .observeOn(Schedulers.newThread())
//                .subscribe((u) -> {
//                    List<Integer> sampleSetIds = Arrays.asList(
//                            415 /*state capitals*/,
//                            100860839 /* IPA */,
//                            118250511 /*french verbs */);
//                    String userValue = mSetField.getText().toString();
//                    Collections.shuffle(sampleSetIds);
//                    long setId = 415;//sampleSetIds.get(0);
//                    try {
//                        setId = Long.valueOf(userValue);
//                    } catch (NumberFormatException e) {
//                        Log.d(TAG, "couldn't make a number out of '" + userValue + "', defaulting to " + setId);
//                    }
//
//                    Log.i(TAG, "Can now look up QSet for content " + setId);
//
//                    mModelService.requestSet(setId);
//
////                    model.getFacts().observe(this, (l)->{
////                        Log.i(TAG,"Can I see this sad pathetic other update? ");
////                        Log.i(TAG, )
////                    });
//                });
//        mHostConnection.getLobbyStateUpdates().observeOn(Schedulers.newThread()).subscribe(
//                (state) -> {
//                    TopLevelViewModel model = ViewModelProviders.of(this).get(TopLevelViewModel.class);
//                    model.processLobbyUpdate(state);
//                },
//                (e) -> Log.e(TAG, "Error updating lobby view model [" + Thread.currentThread().getName() + "] " + e)
//        );
//        mHostConnection.getBoardStateUpdates().observeOn(Schedulers.newThread()).subscribe(
//                (state) -> {
//                    TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
//                    viewModel.processGameUpdate(state);
//                    ensureBoardFragmentUp();
//                },
//                (e) -> Log.e(TAG, "Error updating board view model [" + Thread.currentThread().getName() + "] " + e)
//        );
//        mHostConnection.getStartStatusUpdates().observeOn(Schedulers.newThread()).subscribe(
//                (canStart) -> {
//                    TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
//                    viewModel.setGameState(canStart ? Game.State.CAN_START : Game.State.WAITING);
//                },
//                (e) -> Log.e(TAG, "Error updating start state [" + Thread.currentThread().getName() + "] " + e)
//        );
//    }

//    private void startHostDiscoverabilityWindow(int seconds) {
//        String toastMsg = String.format(getResources().getString(R.string.host_discoverable_toast), seconds);
//        Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
//        mHostButton.setChecked(true);
//        mHostButton.setText(R.string.host_button_discoverable);
//        Single.timer(seconds, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((t) -> { // do I need to worry about the button no longer being around?
//                    mHostButton.setText(R.string.host_button);
//                    mHostButton.setChecked(false);
//                    Log.i(TAG, "Host no longer discoverable after " + seconds + " seconds");
//                });
//    }

//    private void addToGameOptionList(@NonNull BluetoothDevice device, @Nullable String name, int bondState, @NonNull String address) {
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        TextView v = new TextView(this, null, 0, R.style.GameOption);
//
//        StringBuilder sb = new StringBuilder();
//        sb.append(name == null ? "unknown" : name);
//        sb.append(" ");
//        sb.append(bondState == BluetoothDevice.BOND_BONDING ? "[bonding] " : bondState == BluetoothDevice.BOND_BONDED ? "[bonded] " : "");
//        sb.append(address);
//        v.setText(sb.toString());
//        v.setLayoutParams(layoutParams);
//        if (bondState == BluetoothDevice.BOND_BONDED) {
//            mJoinList.addView(v, 0);
//        } else {
//            mJoinList.addView(v);
//        }
//        v.setOnClickListener((view) -> {
//            if (mPlayerConnection.isBound()) {
//                mJoinList.removeAllViews();
//                mPlayerConnection.connectToServer(device, mUsernameField.getText().toString());
//
//                TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
//                viewModel.joinNewGame(name, bondState, address);
//                initPlayerConnectionObservables();
//            }
//        });
//    }


    @Override
    public void onDeviceFound(@NonNull BluetoothDevice device, @Nullable String name, int bondState, @NonNull String address) {
        if (mPlayerBluetoothListener != null) {
            mPlayerBluetoothListener.onDeviceFound(device, name, bondState, address);
        }
        Log.e(TAG, "onDeviceFound  : " + name + " : " + bondState + " : " + address);
//        addToGameOptionList(device, name, bondState, address);
    }

    @Override
    public void requestDiscoverabilityIntent(@NonNull Intent intent) {
        Log.i(TAG, "Starting Discoverability Intent");
        startActivityForResult(intent, REQUEST_DISCOVERABLE_CODE);
    }

    @Override
    public void isDiscoverable(boolean isDiscoverable) {
        if (mPlayerBluetoothListener != null) {
            mPlayerBluetoothListener.isDiscoverable(isDiscoverable);
        }
        Log.e(TAG, "isDiscoverable  : " + isDiscoverable);
    }

    //endregion


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    BehaviorSubject<Boolean> mBoundModelServiceSubject = BehaviorSubject.create();
    private ServiceConnection mModelConnection = new ServiceConnection() {
        CompositeDisposable mDisposable = new CompositeDisposable();

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ModelRetrievalService.LocalBinder binder = (ModelRetrievalService.LocalBinder) service;
            mModelService = binder.getService();

            mDisposable.add(mModelService.getQSetFlowable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((QSet qSet) -> {
                        TopLevelViewModel viewModel = ViewModelProviders.of(StartActivity.this).get(TopLevelViewModel.class);
                        viewModel.processQuizletResults(qSet);

                        Completable.defer(() -> {
                            TopLevelViewModel model = ViewModelProviders.of(StartActivity.this).get(TopLevelViewModel.class);
                            List<Fact> facts = model.getFacts().getValue();
                            Log.i(TAG, "Rebecca, I've gotten my updated facts : " + facts);
                            return Completable.complete();
                        }).subscribeOn(Schedulers.newThread()).subscribe();
                    }))
            ;

            mDisposable.add(mModelService.getQUserFlowable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((QUser qUser) -> {
                        TopLevelViewModel viewModel = ViewModelProviders.of(StartActivity.this).get(TopLevelViewModel.class);
                        viewModel.processQUser(qUser);
                        LongSparseArray<QSet> sets = new LongSparseArray<>();
                        for (QSet set : qUser.recentSets()) {
                            Log.i(TAG, " >> [recent set] " + set.title() + " : " + set.description() + " : " + set.creatorUsername());
                            sets.put(set.id(), set);
                        }
                        for (QSet set : qUser.favoriteSets()) {
                            Log.i(TAG, " >> [favorite set] " + set.title() + " : " + set.description() + " : " + set.creatorUsername());
                            sets.put(set.id(), set);
                        }

                        for (QStudied studied : qUser.studied()) {
                            QSet set = studied.set();
                            Log.i(TAG, " >> [studied set] " + set.title() + " : " + set.description() + " : " + set.creatorUsername());
                            sets.put(set.id(), set);
                        }
                        viewModel.updateSetSummaryData(sets);
                    }))
            ;
            mModelBound = true;
            mBoundModelServiceSubject.onNext(true);
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            if (mDisposable != null) {
                mDisposable.dispose();
            }
            mModelBound = false;
            mBoundModelServiceSubject.onNext(false);
        }
    };


    public PlayerServiceConnection getPlayerConnection(IBluetoothPlayerListener listener) {
        mPlayerBluetoothListener = listener;
        mPlayerConnection.startLooking(this);
        return mPlayerConnection;
    }

    public IModelRetrievalService getModelService() {
        return mModelService;
    }
}
