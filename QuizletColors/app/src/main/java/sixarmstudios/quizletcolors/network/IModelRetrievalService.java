package sixarmstudios.quizletcolors.network;

import android.arch.lifecycle.LifecycleActivity;
import android.support.v4.app.FragmentActivity;

import io.reactivex.Flowable;
import quizlet.QSet;
import quizlet.QUser;

/**
 * Created by rebeccastecker on 6/12/17.
 */


public interface IModelRetrievalService {
    // in theory there will be more... do I want to have different observables for different types
    // of requests or different objects? ... how do I indicate the results of a search for example?

    /**
     * @return observable that never finishes
     */
    Flowable<QSet> getQSetFlowable();

    /**
     * @return observable that emits the user details of the latest user to auth with the app
     */
    Flowable<QUser> getQUserFlowable();

    /**
     * Kicks off a server request. When the set is loaded, you can find the results over at
     * {@link IModelRetrievalService#getQSetFlowable()}
     */
    void requestSet(long localId);

    String getOauthUrl();

    String getSecretCode();

    String getRedirectUrl();

    void handelOauthCode(LifecycleActivity lifecycleActivity, String authCode);

    void refreshSummary();

    void restoreQuizletInfo(String accessCode, String username);
}