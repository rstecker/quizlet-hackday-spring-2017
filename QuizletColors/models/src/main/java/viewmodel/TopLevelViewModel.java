package viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import database.AppDatabase;
import gamelogic.BoardState;
import gamelogic.LobbyState;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import quizlet.QSet;
import quizlet.QTerm;
import ui.Fact;
import ui.Game;
import ui.Option;

/**
 * Created by rebeccastecker on 6/11/17.
 */

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

    public void resetGame() {
        Log.i(TAG, "Requesting a reset");
        Completable.defer(
                () -> {
                    Log.i(TAG, "Clearing out DB");
                    mAppDatabase.playerDao().clearGame();
                    mAppDatabase.gameDao().clearGame();
                    mAppDatabase.factDao().clearGame();
                    mAppDatabase.optionsDao().clearGame();
                    mAppDatabase.goodMovesDao().clearMoves();
                    mAppDatabase.badMovesDao().clearMoves();
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void setUpNewGame(final @NonNull String hostName) {
        Completable.defer(
                () -> {
                    Game newGame = new Game();
                    newGame.initForHost(hostName);
                    mAppDatabase.gameDao().insertAll(newGame);

                    // TODO : somehow actually get content from Quizlet! In the mean time....
                    List<Fact> mockContent = new ArrayList<>();
                    for (int i = 1; i < 21; ++i) {
                        String question = "q" + i;
                        String answer = "a" + i;
//                        for (int j = 0; j < Math.random() * 5; ++j) {
//                            question += " q"+i;
//                        }
//                        for (int j = 0; j < Math.random() * 10; ++j) {
//                            answer += " a"+i;
//                        }

                        mockContent.add(new Fact(-1, question, answer));
                    }
//                    mAppDatabase.factDao().insertAll(mockContent);

                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void moveSubmitted(String option, String color) {
        Completable.defer(
                () -> {
                    mAppDatabase.gameDao().setSelectedColor(null);
                    mAppDatabase.gameDao().setSelectedOption(null);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void joinNewGame(@Nullable String name, int bondState, @NonNull String address) {
        Completable.defer(
                () -> {
                    Game newGame = new Game();
                    newGame.initForPlayer(name, bondState, address);
                    mAppDatabase.gameDao().insertAll(newGame);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void setGameState(Game.State state) {
        Completable.defer(
                () -> {
                    mAppDatabase.gameDao().setGameState(Game.State.stateToString(state));
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }


    public void processLobbyUpdate(LobbyState state) {
        Completable.defer(
                () -> {
                    mAppDatabase.playerDao().insertAll(state.players());
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
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
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void processQuizletResults(QSet qSet) {
        Completable.defer(
                () -> {
                    List<Fact> content = new ArrayList<>();
                    for (QTerm t : qSet.terms()) {
                        content.add(new Fact(qSet.id(), t.word(), t.definition()));
                    }
                    Log.i(TAG, "Adding "+content.size()+" Quizlet Facts into the db");
                    mAppDatabase.factDao().insertAll(content);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
