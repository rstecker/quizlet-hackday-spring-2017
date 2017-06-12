package ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import database.AppDatabase;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class BoardViewModel extends AndroidViewModel {
    public static final String TAG = BoardViewModel.class.getSimpleName();
    private AppDatabase mAppDatabase;

    public BoardViewModel(Application application) {
        super(application);
        mAppDatabase = AppDatabase.getDatabase(this.getApplication());
    }

    public LiveData<List<Player>> getPlayers() {
        return mAppDatabase.playerDao().getAll();
    }
    public LiveData<List<Game>> getGame() {
        return mAppDatabase.gameDao().getGame();
    }

    public void processGameUpdate(BoardState state) {
        Completable.defer(
                () -> {
        // TODO : keep an eye on player updates - we do have player info in the board state
                    mAppDatabase.gameDao().setCurrentQuestion(state.question());
                    mAppDatabase.gameDao().setOptions(
                            grabOption(state.options(), 0),
                            grabOption(state.options(), 1),
                            grabOption(state.options(), 2),
                            grabOption(state.options(), 3)
                    );
                    mAppDatabase.gameDao().setGameState(Game.State.stateToString(Game.State.PLAYING));
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private String grabOption(List<String> options, int optionNumber) {
        if (optionNumber < options.size()) {
            return options.get(optionNumber);
        }
        return null;
    }
}
