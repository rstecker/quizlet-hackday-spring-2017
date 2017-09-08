package sixarmstudios.quizletcolors.network;

import android.app.Service;
import android.arch.lifecycle.LifecycleActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.LongSparseArray;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.schedulers.Schedulers;
import quizlet.QSet;
import quizlet.QUser;
import sixarmstudios.quizletcolors.BuildConfig;

/**
 * Created by rebeccastecker on 6/12/17.
 */

public class ModelRetrievalService extends Service implements IModelRetrievalService {
    public static final String TAG = ModelRetrievalService.class.getSimpleName();
    private static final String CLIENT_ID_ARG = "clientIdArg";
    private static final String ENCODED_STRING_ARG = "encodedStringArg";
    private static final String REDIRECT_URL_ARG = "redirectUrlArg";
    private static final String SECRET_CODE_ARG = "secretCodeArg";
    private final LongSparseArray<QSet> mQSetMap = new LongSparseArray<>();
    private ApiClient mClient;
    private final IBinder mBinder = new LocalBinder();
    private final BehaviorProcessor<QSet> mCachedQSet = BehaviorProcessor.create();

    String mClientId;
    String mEncodedString;
    String mRedirectUrl;
    String mSecretCode;

    public static Intent startIntent(Context context, String fakeClientId) {
        Intent intent = new Intent(context, ModelRetrievalService.class);
//        BuildConfig
        intent.putExtra(CLIENT_ID_ARG, BuildConfig.QUIZLET_API_KEY);
        intent.putExtra(REDIRECT_URL_ARG, BuildConfig.QUIZLET_REDIRECT_URL_KEY);
        intent.putExtra(SECRET_CODE_ARG, BuildConfig.QUIZLET_SECRET_CODE_KEY);
        intent.putExtra(ENCODED_STRING_ARG, BuildConfig.QUIZLET_ENCODED_STRING_KEY);
        return intent;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public IModelRetrievalService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ModelRetrievalService.this;
        }
    }

    private void readFromBundle(Intent intent) {
        mClientId = intent.getStringExtra(CLIENT_ID_ARG);
        mEncodedString = intent.getStringExtra(ENCODED_STRING_ARG);
        mRedirectUrl = intent.getStringExtra(REDIRECT_URL_ARG);
        mSecretCode = intent.getStringExtra(SECRET_CODE_ARG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, Thread.currentThread().getName() + "] I see us trying to create... lets only do this once, ok?");
        // subscription used to be here

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.i(TAG, Thread.currentThread().getName() + "] I have a null intent... maybe I should seriously think about shutting down? (flags [" + flags + "], startId [" + startId + "])");
            // TODO : consider killing service right now
            return START_STICKY;
        }
        readFromBundle(intent);
        mClient = new ApiClient(mEncodedString);
        mClient.getQSetFlowable().subscribe((qSet) -> mQSetMap.put(qSet.id(), qSet));
        Log.i(TAG, Thread.currentThread().getName() + "] I see us trying to start w/ client id (flags [" + flags + "], startId [" + startId + "]) : " + intent.getStringExtra(CLIENT_ID_ARG));
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onBind request : " + intent);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onUNbind request : " + intent);
        // TODO : start countdown timer to shut down service
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onREbind request : " + intent);
        // TODO : stop countdown timer, keep service
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, TAG + " done", Toast.LENGTH_SHORT).show();
    }

    //region interface methods

    @Override
    public Flowable<QSet> getQSetFlowable() {
        // in theory I would own this observable and select between my cache and the API to find it
        return Flowable.merge(mCachedQSet, mClient.getQSetFlowable());
    }
    @Override
    public Flowable<QUser> getQUserFlowable() {
        // in theory I would own this observable and select between my cache and the API to find it
        return mClient.getQUserFlowable();
    }

    @Override
    public void requestSet(final long setId) {
        Log.i(TAG, "Requesting set " + setId);
        QSet cachedSet = mQSetMap.get(setId);
        if (cachedSet != null) {
            mCachedQSet.onNext(cachedSet);
        } else {
            new Thread(() -> mClient.fetchSet(setId, mClientId)).start();
        }
    }

    @Override
    public String getOauthUrl() {
        return "https://quizlet.com/authorize?response_type=code&client_id=" + mClientId + "&scope=read&state=" + mSecretCode;
    }

    @Override
    public void handelOauthCode(LifecycleActivity context, String authCode) {
        Completable
                .defer(() -> {
                    mClient.handleOAuthCode(context, authCode, mRedirectUrl);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe()
        ;
    }

    @Override
    public String getSecretCode() {
        return mSecretCode;
    }

    @Override
    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    @Override public void refreshSummary() {
        Completable
                .defer(() -> {
                    mClient.updateUserInfo();
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe()
                ;
    }

    @Override public void restoreQuizletInfo(String accessCode, String username) {
        if (StringUtils.isEmpty(username)) {
            return;
        }
        mClient.setRestoredState(accessCode, username);
    }
    //endregion
}