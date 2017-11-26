package database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import appstate.AppState;
import ui.BadMove;
import ui.GoodMove;
import ui.Option;
import ui.Fact;
import ui.Game;
import ui.Player;
import ui.SetSummary;

/**
 * Created by rebeccastecker on 6/11/17.
 */

@Database(entities = {Player.class, Game.class, Fact.class, Option.class, GoodMove.class,
        BadMove.class, AppState.class, SetSummary.class}, version = 8)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlayerDao playerDao();
    public abstract GameDao gameDao();
    public abstract FactDao factDao();
    public abstract OptionDao optionsDao();
    public abstract GoodMoveDao goodMovesDao();
    public abstract BadMoveDao badMovesDao();
    public abstract ApplicationStateDao applicationStateDao();
    public abstract SetSummaryDao setSummaryDao();

    private static AppDatabase mInstance;


    public static AppDatabase getDatabase(Context context) {
        if (mInstance == null) {
            mInstance = Room
                    .databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class, "q_color_db"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return mInstance;
    }

}