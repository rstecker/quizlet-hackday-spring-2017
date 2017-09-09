package sixarmstudios.quizletcolors.ui.setup;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bluetooth.core.IBluetoothPlayerListener;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import appstate.AppState;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.schedulers.Schedulers;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.StartActivity;
import sixarmstudios.quizletcolors.connections.PlayerServiceConnection;
import viewmodel.TopLevelViewModel;

/**
 * Created by rebeccastecker on 9/7/17.
 */
@ParametersAreNonnullByDefault
public class LookingForGameFragment extends LifecycleFragment implements IBluetoothPlayerListener {
    public static final String TAG = LookingForGameFragment.class.getSimpleName();
    @LayoutRes public static final int LAYOUT_ID = R.layout.fragment_looking_for_game;

    private PlayerServiceConnection mPlayerConnection;
    private Set<String> mSeenSets = new HashSet<>();

    @BindView(R.id.join_option_list) LinearLayout mJoinList;
    @BindView(R.id.username_text_field) EditText mUsernameField;


    public static LookingForGameFragment newInstance() {
        LookingForGameFragment fragment = new LookingForGameFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT_ID, container, false);
        ButterKnife.bind(this, view);
        mJoinList.removeAllViews();
        mUsernameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override public void afterTextChanged(Editable editable) {
                updateEnabledState();
            }
        });
        TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
        viewModel.getAppState().observe(this, appStates -> {
            if (appStates == null || appStates.isEmpty()) {
                return;
            }
            AppState state = appStates.get(0);
            if (StringUtils.isNoneEmpty(state.qUsername)) {
                mUsernameField.setText(state.qUsername);
                mUsernameField.setHint(state.qUsername);
            } else {
                mUsernameField.setText("FooBar");








            }
        });
        return view;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");
        mPlayerConnection = ((StartActivity) context).getPlayerConnection(this);
    }

    @Override public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onDeviceFound(@NonNull BluetoothDevice device, @Nullable String name, int bondState, @NonNull String address) {
        Log.i(TAG, "onDeviceFound  : " + name + " : " + bondState + " : " + address);
        if (mSeenSets.contains(address) || StringUtils.isEmpty(address)) {
            return;
        }
        mSeenSets.add(address);
        addToGameOptionList(device, name, bondState, address);
    }

    @Override public void isDiscoverable(boolean isDiscoverable) {
        Log.i(TAG, "isDiscoverable  : " + isDiscoverable);
    }

    private void updateEnabledState() {
        boolean noName = StringUtils.isEmpty(mUsernameField.getText());
        mJoinList.setVisibility(noName ? View.GONE : View.VISIBLE);
    }

    private void addToGameOptionList(@NonNull BluetoothDevice device, @Nullable String name, int bondState, @NonNull String address) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView v = new TextView(getContext(), null, 0, R.style.GameOption);

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
            mJoinList.removeAllViews();
            mPlayerConnection.connectToServer(device, mUsernameField.getText().toString());

            TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
            viewModel.joinNewGame(name, bondState, address);
        });
    }
}
