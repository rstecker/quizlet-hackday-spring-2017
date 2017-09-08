package viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import appstate.PlayerState;
import database.AppDatabase;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import ui.Fact;
import ui.Game;
import ui.Player;
import ui.SetSummary;

/**
 * Created by rebeccastecker on 6/10/17.
 */
@ParametersAreNonnullByDefault
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

    public LiveData<List<SetSummary>> getSetSummary(long setId) {
        return mAppDatabase.setSummaryDao().getSetSummary(setId);
    }

    public LiveData<List<Fact>> getFacts(long setId) {
        return mAppDatabase.factDao().getFactsForSet(setId);
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

}
