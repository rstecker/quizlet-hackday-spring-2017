package viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.Nullable;

import java.util.List;

import database.AppDatabase;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import ui.BadMove;
import ui.GoodMove;
import ui.Option;
import ui.Game;
import ui.Player;

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
    public LiveData<List<BadMove>> getMyBadMoves() {
        return mAppDatabase.badMovesDao().getYourMostRecent();
    }
    public LiveData<List<GoodMove>> getMyGoodMoves() {
        return mAppDatabase.goodMovesDao().getYourMostRecent();
    }

    public LiveData<List<Option>> getOptions() {
        return mAppDatabase.optionsDao().getAll();
    }

    public void setSubmittedOption(@Nullable String option) {
        Completable.defer(
                () -> {
                    mAppDatabase.gameDao().setSelectedOption(option);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    public void setSelectedPlayer(@Nullable String selectedColor) {
        Completable.defer(
                () -> {
                    mAppDatabase.gameDao().setSelectedColor(selectedColor);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }
}
