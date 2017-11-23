package sixarmstudios.quizletcolors.ui.setup;

import android.app.Activity;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import appstate.AppState;
import appstate.PlayerState;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.StartActivity;
import studioes.arm.six.partskit.CompassRose;
import studioes.arm.six.partskit.Player;
import viewmodel.TopLevelViewModel;

import static sixarmstudios.quizletcolors.StartActivity.REQUEST_ENABLE_BT;

/**
 * Holds the user till they've got the correct permissions set up & bluetooth is on.
 * Lets them kick off as a Quizlet user or as an anonymous user
 */

public class StartFragment extends LifecycleFragment {
    public static final String TAG = StartFragment.class.getSimpleName();
    @LayoutRes
    public static final int LAYOUT_ID = R.layout.fragment_start;

    @BindView(R.id.has_permissions)
    TextView mHasPermissionsTxtView;
    @BindView(R.id.has_bluetooth)
    TextView mHasBluetoothTxtView;
    @BindView(R.id.game_start_options)
    View mStartGameOptions;
    @BindView(R.id.start_with_quizlet_btn)
    TextView mStartQuizletButton;
    @BindView(R.id.start_anonymous_btn)
    TextView mStartAnonymousButton;
    @BindView(R.id.start_compass_rose)
    CompassRose mRose;

    private Disposable mDisposable;


    String mQuizletUsername = null;

    public static StartFragment newInstance() {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT_ID, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mDisposable.dispose();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkHealth();
        TopLevelViewModel boardViewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
        boardViewModel.getAppState().observe(this, (appStates -> {
            if (appStates == null || appStates.isEmpty()) {
                mQuizletUsername = null;
                updateQuizletUserInfo();
                return;
            }
            AppState state = appStates.get(0);
            mQuizletUsername = state.qUsername;
            updateQuizletUserInfo();
        }));

        prepRose();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case StartActivity.REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Permission denied");
                            return;
                        }
                    }
                    Toast.makeText(getContext(), R.string.permission_approved, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Permission granted");
                    checkHealth();
                }
                break;
            default:
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getContext(), R.string.bluetooth_denied, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Bluetooth request denied");
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getContext(), R.string.bluetooth_approved, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Bluetooth enabled");
            checkHealth();
        }
    }

    @OnClick({R.id.has_bluetooth, R.id.has_permissions})
    public void handlePermissionCheckClick() {
        checkHealth();
    }


    @OnClick(R.id.start_anonymous_btn)
    public void handleStartAnonymousClick() {
        TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
        viewModel.updatePlayerState(PlayerState.FIND_GAME);
        mStartAnonymousButton.setOnContextClickListener(null);

    }

    @OnClick(R.id.start_with_quizlet_btn)
    public void handleQuizletStart() {
        if (StringUtils.isEmpty(mQuizletUsername)) {
            kickOffAuthFlow();
        } else {
            TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
            viewModel.updatePlayerState(PlayerState.FIND_SET);
            mStartQuizletButton.setOnContextClickListener(null);
        }
    }

    private void updateQuizletUserInfo() {
        if (StringUtils.isEmpty(mQuizletUsername)) {
            mStartQuizletButton.setText(R.string.auth_with_quizlet);
            mStartAnonymousButton.setText(R.string.play_anonymously);
        } else {
            mStartQuizletButton.setText(getResources().getString(R.string.use_quizlet_host, mQuizletUsername));
            mStartAnonymousButton.setText(getResources().getString(R.string.use_quizlet_play, mQuizletUsername));
        }
    }

    private void checkHealth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        mHasPermissionsTxtView.setVisibility(View.INVISIBLE);
        mStartGameOptions.setVisibility(View.INVISIBLE);

        if (adapter == null) {
            // Device does not support Bluetooth
            Log.e(TAG, "Bluetooth not possible on device, no go!");
            mHasBluetoothTxtView.setText(R.string.no_bluetooth_error);
            return;
        }
        if (!adapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not enabled, no go!");
            mHasBluetoothTxtView.setText(R.string.bluetooth_off);

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        mHasBluetoothTxtView.setText(R.string.bluetooth_on);
        mHasPermissionsTxtView.setVisibility(View.VISIBLE);

        if (!verifyPermission()) {
            Log.e(TAG, "Missing permissions, no go!");
            mHasPermissionsTxtView.setText(R.string.missing_permissions);
            return;
        }

        mHasPermissionsTxtView.setText(R.string.has_permissions);
        mStartGameOptions.setVisibility(View.VISIBLE);
    }

    /**
     * @return true if the app has all the permissions it needs.
     */
    private boolean verifyPermission() {
        int accessCoarseLocation = getContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        List<String> listRequestPermission = new ArrayList<String>();
        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            requestPermissions(strRequestPermission, StartActivity.REQUEST_PERMISSIONS_CODE);
        }
        return listRequestPermission.isEmpty();
    }

    private void kickOffAuthFlow() {
        TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
        viewModel.updatePlayerState(PlayerState.ATTEMPT_OAUTH);
    }

    private void prepRose() {
        mRose.setPlayer(new Player(
                "play now!",
                CompassRose.RoseColor.BLUE,
                0,
                false, false,
                CompassRose.PLAYER_SHAPE_DRAWABLE_RES[0]
        ));
        mRose.setOnClickListener((l) -> mRose.boop());
        mRose.setEnergy(0.5f);
        mDisposable = Observable.interval(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    mRose.setEnergy((float) Math.random());
                    if (Math.random() < 0.2) {
                        mRose.boop();
                    }
                    if (Math.random() < 0.3) {
                        mRose.reward();
                    }
                });
    }
}
