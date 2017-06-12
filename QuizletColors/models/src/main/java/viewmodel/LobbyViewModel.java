package viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import database.AppDatabase;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import ui.Fact;
import ui.Game;
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

    public LiveData<List<Fact>> getFacts() {
        return mAppDatabase.factDao().getFacts();
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

}
