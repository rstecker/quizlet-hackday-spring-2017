package sixarmstudios.quizletcolors.network;

import android.support.v7.app.AppCompatActivity;

import io.reactivex.Completable;
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
    @Deprecated
    void requestSet(long localId);

    String getOauthUrl();

    String getRedirectUrl();

    void handelOauthCode(AppCompatActivity lifecycleActivity, String authCode);

    void refreshSummary();

    void restoreQuizletInfo(String accessCode, String username);

    Completable fetchSetDetails(long setId);
}