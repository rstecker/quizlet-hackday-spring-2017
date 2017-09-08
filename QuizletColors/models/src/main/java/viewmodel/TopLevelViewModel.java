package viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import appstate.AppState;
import appstate.PlayerState;
import database.AppDatabase;
import gamelogic.BoardState;
import gamelogic.LobbyState;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import quizlet.QSet;
import quizlet.QTerm;
import quizlet.QUser;
import ui.Fact;
import ui.Game;
import ui.Option;
import ui.SetSummary;

/**
 * Created by rebeccastecker on 6/11/17.
 */
@ParametersAreNonnullByDefault
public class TopLevelViewModel extends AndroidViewModel {
    public static final String TAG = TopLevelViewModel.class.getSimpleName();
    private AppDatabase mAppDatabase;

    public TopLevelViewModel(Application application) {
        super(application);
        mAppDatabase = AppDatabase.getDatabase(this.getApplication());
    }


    public LiveData<List<Game>> getGame() {
        return mAppDatabase.gameDao().getGame();
    }

    public LiveData<List<Fact>> getFacts() {
        return mAppDatabase.factDao().getFacts();
    }

    public LiveData<List<AppState>> getAppState() {
        return mAppDatabase.applicationStateDao().getGame();
    }

    public LiveData<List<SetSummary>> getSetSummaries() {
        return mAppDatabase.setSummaryDao().getAllByQuizletUse();
    }

    public void resetGame() {
        Log.i(TAG, "Requesting a game reset");
        Completable.defer(
                () -> {
                    Log.i(TAG, "Clearing out DB");
                    mAppDatabase.playerDao().clearGame();
                    mAppDatabase.gameDao().clearGame();
                    mAppDatabase.optionsDao().clearGame();
                    mAppDatabase.goodMovesDao().clearMoves();
                    mAppDatabase.badMovesDao().clearMoves();
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    @Deprecated
    public void setUpNewGame(final @NonNull String hostName) {
        Completable.defer(
                () -> {
                    Game newGame = new Game();
//                    newGame.initForHost(hostName);
                    mAppDatabase.gameDao().insertAll(newGame);

                    // TODO : somehow actually get content from Quizlet! In the mean time....
                    List<Fact> mockContent = new ArrayList<>();
                    mockContent.add(new Fact(-1, "Alabama", "Montgomery"));
                    mockContent.add(new Fact(-1, "Alaska", "Juneau"));
                    mockContent.add(new Fact(-1, "Arizona", "Phoenix"));
                    mockContent.add(new Fact(-1, "Arkansas", "Little Rock"));
                    mockContent.add(new Fact(-1, "California", "Sacramento"));
                    mockContent.add(new Fact(-1, "Colorado", "Denver"));
                    mockContent.add(new Fact(-1, "Connecticut", "Hartford"));
                    mockContent.add(new Fact(-1, "Delaware", "Dover"));
                    mockContent.add(new Fact(-1, "Florida", "Tallahassee"));
                    mockContent.add(new Fact(-1, "Georgia", "Atlanta"));
                    mockContent.add(new Fact(-1, "Hawaii", "Honolulu"));
                    mockContent.add(new Fact(-1, "Idaho", "Boise"));
                    mockContent.add(new Fact(-1, "Illinois", "Springfield"));
                    mockContent.add(new Fact(-1, "Indiana", "Indianapolis"));
                    mockContent.add(new Fact(-1, "Iowa", "Des Moines"));
                    mockContent.add(new Fact(-1, "Kansas", "Topeka"));
                    mockContent.add(new Fact(-1, "Kentucky", "Frankfort"));
                    mockContent.add(new Fact(-1, "Louisiana", "Baton Rouge"));
                    mockContent.add(new Fact(-1, "Maine", "Augusta"));
                    mockContent.add(new Fact(-1, "Maryland", "Annapolis"));
                    mockContent.add(new Fact(-1, "Massachusetts", "Boston"));
                    mockContent.add(new Fact(-1, "Michigan", "Lansing "));
                    mockContent.add(new Fact(-1, "Minnesota", "St.Paul "));
                    mockContent.add(new Fact(-1, "Mississippi", "Jackson "));
                    mockContent.add(new Fact(-1, "Missouri", "Jefferson City "));
                    mockContent.add(new Fact(-1, "Montana", "Helena "));
                    mockContent.add(new Fact(-1, "Nebraska", "Lincoln "));
                    mockContent.add(new Fact(-1, "Nevada", "Carson City "));
                    mockContent.add(new Fact(-1, "New Hampshire", "-Concord "));
                    mockContent.add(new Fact(-1, "New Jersey", "Trenton "));
                    mockContent.add(new Fact(-1, "New Mexico", "Santa Fe "));
                    mockContent.add(new Fact(-1, "New York", "Albany "));
                    mockContent.add(new Fact(-1, "North Carolina", "Raleigh "));
                    mockContent.add(new Fact(-1, "North Dakota", "Bismarck "));
                    mockContent.add(new Fact(-1, "Ohio", "Columbus "));
                    mockContent.add(new Fact(-1, "Oklahoma", "Oklahoma City "));
                    mockContent.add(new Fact(-1, "Oregon", "Salem "));
                    mockContent.add(new Fact(-1, "Pennsylvania", "Harrisburg "));
                    mockContent.add(new Fact(-1, "Rhode Island", "Providence "));
                    mockContent.add(new Fact(-1, "South Carolina", "Columbia "));
                    mockContent.add(new Fact(-1, "South Dakota", "Pierre "));
                    mockContent.add(new Fact(-1, "Tennessee", "Nashville "));
                    mockContent.add(new Fact(-1, "Texas", "Austin "));
                    mockContent.add(new Fact(-1, "Utah", "Salt Lake City "));
                    mockContent.add(new Fact(-1, "Vermont", "Montpelier "));
                    mockContent.add(new Fact(-1, "Virginia", "Richmond "));
                    mockContent.add(new Fact(-1, "Washington", "Olympia "));
                    mockContent.add(new Fact(-1, "West Virginia", "Charleston "));
                    mockContent.add(new Fact(-1, "Wisconsin", "Madison "));
                    mockContent.add(new Fact(-1, "Wyoming", "Cheyenne "));

//                    for (int i = 1; i < 21; ++i) {
//                        String question = "q" + i;
//                        String answer = "a" + i;
////                        for (int j = 0; j < Math.random() * 5; ++j) {
////                            question += " q"+i;
////                        }
////                        for (int j = 0; j < Math.random() * 10; ++j) {
////                            answer += " a"+i;
////                        }
//
//                        mockContent.add(new Fact(-1, question, answer));
//                    }
                    Log.i(TAG, "Adding mock content : " + mockContent.size() + " to db. Can we see this?");
//                    mAppDatabase.factDao().insertAll(mockContent);

                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void moveSubmitted(String option, String color) {
        Completable.defer(
                () -> {
                    mAppDatabase.gameDao().setSelectedColor(null);
                    mAppDatabase.gameDao().setSelectedOption(null);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void joinNewGame(@Nullable String name, int bondState, @NonNull String address) {
        Completable.defer(
                () -> {

                    Game newGame = new Game();
                    newGame.initForPlayer(name, bondState, address);
                    mAppDatabase.gameDao().insertAll(newGame);
                    mAppDatabase.applicationStateDao().updatePlayerState(PlayerState.LOBBY.toDBVal());
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void setGameState(Game.State state) {
        Completable.defer(
                () -> {
                    mAppDatabase.gameDao().setGameState(Game.State.stateToString(state));
                    if (state == Game.State.PLAYING || state == Game.State.START) {
                        mAppDatabase.applicationStateDao().updatePlayerState(PlayerState.PLAYING.toDBVal());
                    }
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }


    public void processLobbyUpdate(LobbyState state) {
        Completable.defer(
                () -> {
                    mAppDatabase.playerDao().insertAll(state.players());
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void processGameUpdate(BoardState state) {
        Completable.defer(
                () -> {
                    // TODO : keep an eye on player updates - we do have player info in the board state
                    mAppDatabase.gameDao().setCurrentQuestion(state.question());
                    List<Option> options = new ArrayList<>();
                    for (int i = 0; i < state.options().size(); ++i) {
                        options.add(new Option(i, state.options().get(i)));
                    }
                    if (state.goodMove() != null) {
                        Log.d(TAG, "Good move detected when reading in state update : " + state.goodMove());
                        mAppDatabase.goodMovesDao().insertAll(state.goodMove());
                    }
                    if (state.badMove() != null) {
                        Log.d(TAG, "Bad move detected when reading in state update: " + state.badMove());
                        mAppDatabase.badMovesDao().insertAll(state.badMove());
                    }
                    mAppDatabase.optionsDao().insertAll(options);
                    mAppDatabase.gameDao().setGameState(Game.State.stateToString(Game.State.PLAYING));
                    mAppDatabase.applicationStateDao().updatePlayerState(PlayerState.PLAYING.toDBVal());
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void markSetAsSynced(long setId) {
        Completable.fromRunnable(
                () -> {
                    mAppDatabase.setSummaryDao().updateSyncTimestamp(setId, new Date().getTime());
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void processTermsFromQuizletSet(QSet qSet) {
        Completable.fromRunnable(
                () -> {
                    List<Fact> content = new ArrayList<>();
                    for (QTerm qTerm : qSet.terms()) {
                        content.add(new Fact(qSet.id(), qTerm.word(), qTerm.definition()));
                    }
                    mAppDatabase.factDao().insertAll(content);
                })
                .subscribeOn(Schedulers.newThread())
                .doOnError((e) -> Log.e(TAG, "Error encountered while updating QSet terms.Can I see it?"))
                .doOnComplete(() -> Log.i(TAG, "Can I see my db stuff NOW? Has completed"))
                .subscribe()
        ;
    }

    public void processQUser(QUser qUser) {
        // TODO : update payment level in DB?
    }

    /**
     * Should only happen once per app install. Initial setup.
     */
    public void initApplication(PlayerState playerState) {
        Completable.defer(
                () -> {
                    AppState appState = new AppState();
                    appState.playState = playerState.toDBVal();
                    mAppDatabase.applicationStateDao().clearState();
                    mAppDatabase.applicationStateDao().insertAll(appState);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void updatePlayerState(PlayerState playerState) {
        Completable.defer(
                () -> {
                    mAppDatabase.applicationStateDao().updatePlayerState(playerState.toDBVal());
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void updateAppState(AppState appState) {
        Completable.defer(
                () -> {
                    mAppDatabase.applicationStateDao().update(appState);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void updateQuizletData(String token, String username) {
        Completable.defer(
                () -> {
                    mAppDatabase.applicationStateDao().updatePlayerStateWithQuizletAuth(PlayerState.FIND_SET.toDBVal(), token, username);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void updateSetSummaryData(LongSparseArray<QSet> sets) {
        Completable.fromRunnable(
                () -> {
                    List<SetSummary> summaries = new ArrayList<>();
                    for (int i = 0; i < sets.size(); ++i) {
                        QSet qset = sets.valueAt(i);
                        summaries.add(new SetSummary(qset));
                    }
                    Log.i(TAG, "Updating db with " + summaries.size() + " set summaries");
                    mAppDatabase.setSummaryDao().insertAll(summaries);
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void startHostingGame(long setId, String hostName) {
        Completable.fromRunnable(
                () -> {
                    Game newGame = new Game();
                    newGame.initForHost(hostName, setId);
                    mAppDatabase.gameDao().insertAll(newGame);
                    mAppDatabase.applicationStateDao().updatePlayerState(PlayerState.LOBBY.toDBVal());
                    mAppDatabase.applicationStateDao().updateCurrentSetId(setId);
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();

    }
}
