package sixarmstudios.quizletcolors.ui.setup;

import android.app.Activity;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.core.IBluetoothHostListener;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import appstate.AppState;
import appstate.PlayerState;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.StartActivity;
import sixarmstudios.quizletcolors.connections.HostServiceConnection;
import sixarmstudios.quizletcolors.network.IModelRetrievalService;
import ui.SetSummary;
import viewmodel.TopLevelViewModel;

import static sixarmstudios.quizletcolors.StartActivity.REQUEST_DISCOVERABLE_CODE;

/**
 * Created by rebeccastecker on 9/7/17.
 */

public class LookingForSetFragment extends LifecycleFragment implements SetSummaryAdapter.Callback, IBluetoothHostListener {
    public static final String TAG = LookingForSetFragment.class.getSimpleName();
    @LayoutRes public static final int LAYOUT_ID = R.layout.fragment_looking_for_set;

    @BindView(R.id.username_text_field) EditText mUsernameField;
    @BindView(R.id.welcome_host_view) TextView mWelcomeTxtView;
    @BindView(R.id.set_list) RecyclerView mSetList;
    @BindView(R.id.refresh_overview) ImageView mRefreshList;
    SetSummaryAdapter mAdapter;
    HostServiceConnection mHostConnection;
    IModelRetrievalService mModelService;

    public static LookingForSetFragment newInstance() {
        LookingForSetFragment fragment = new LookingForSetFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT_ID, container, false);
        ButterKnife.bind(this, view);
        TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
        viewModel.getAppState().observe(this, this::handleVMUpdates);
        viewModel.getSetSummaries().observe(this, this::handleSetUpdates);
        mAdapter = new SetSummaryAdapter(this);
        mSetList.setAdapter(mAdapter);
        mSetList.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mModelService = ((StartActivity) context).getModelService();
        mHostConnection = ((StartActivity) context).getHostConnection();
    }

    @OnClick(R.id.refresh_overview)
    public void handleRefreshList() {
        //have refresh rotate on click
        Animation anim = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(1000);
        mRefreshList.startAnimation(anim);

        mModelService.refreshSummary();
    }

    //region Callback interface for Adapter

    @Override public Completable sync(long setId) {
        return mModelService.fetchSetDetails(setId);
    }

    @Override public void start(long setId) {
        // FIXME : the listener should probably be the activity, not the fragment? OR, the fragment should block advancement to lobby till hosting
        String hostName = mHostConnection.startHosting(this, mUsernameField.getText().toString());
        if (StringUtils.isEmpty(hostName)) {
            Toast.makeText(getContext(), R.string.no_host_name, Toast.LENGTH_SHORT).show();
            return;
        }
        TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
        viewModel.startHostingGame(setId, hostName);
    }


    //endregion

    private void handleVMUpdates(List<AppState> states) {
        if (states == null || states.isEmpty()) {
            return;
        }
        AppState state = states.get(0);
        mWelcomeTxtView.setText(getResources().getString(R.string.host_welcome, state.qUsername));
        mUsernameField.setText(state.qUsername);
        mUsernameField.setHint(state.qUsername);
    }

    private void handleSetUpdates(List<SetSummary> summaries) {
        Log.i(TAG, "I see incoming summaries : " + summaries);
        if (summaries == null || summaries.isEmpty()) {
            return;
        }
        mAdapter.processLiveUpdate(summaries);
    }

    @Override public void requestDiscoverabilityIntent(@NonNull Intent intent) {
        Log.i(TAG, "Starting Discoverability Intent");
        startActivityForResult(intent, REQUEST_DISCOVERABLE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DISCOVERABLE_CODE) {
            if (resultCode <= 0) {
                Log.e(TAG, "Denied discoverability");
                Toast.makeText(getContext(), R.string.discoverability_denied, Toast.LENGTH_LONG).show();
            } else {
                Log.i(TAG, "Discoverable mode enabled for " + resultCode + " seconds");
//                startHostDiscoverabilityWindow(resultCode);
            }
        }
    }

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

}
