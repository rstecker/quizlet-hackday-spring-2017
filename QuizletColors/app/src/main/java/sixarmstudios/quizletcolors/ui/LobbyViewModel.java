package sixarmstudios.quizletcolors.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import database.AppDatabase;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import ui.Game;
import ui.LobbyState;
import ui.Player;

/**
 * Created by rebeccastecker on 6/10/17.
 */

public class LobbyViewModel extends AndroidViewModel {
    public static final String TAG = LobbyViewModel.class.getSimpleName();
    private AppDatabase mAppDatabase;

    public LobbyViewModel(Application application) {
        super(application);
        mAppDatabase = AppDatabase.getDatabase(this.getApplication());
    }

    public LiveData<List<Player>> getPlayers() {
        return mAppDatabase.playerDao().getAll();
    }

    public LiveData<List<Game>> getGame() {
        return mAppDatabase.gameDao().getGame();
    }

    public void resetGame() {
        Completable.defer(
                () -> {
                    mAppDatabase.playerDao().clearGame();
                    mAppDatabase.gameDao().clearGame();
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void processLobbyUpdate(LobbyState state) {
        mAppDatabase.playerDao().insertAll(state.players());
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

    public void setUpNewGame(@NonNull String hostName) {
        Completable.defer(
                () -> {
                    Game newGame = new Game();
                    newGame.initForHost(hostName);
                    mAppDatabase.gameDao().insertAll(newGame);
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

}
