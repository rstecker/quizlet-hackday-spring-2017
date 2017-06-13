package database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import ui.BadMove;
import ui.GoodMove;
import ui.Option;
import ui.Fact;
import ui.Game;
import ui.Player;

/**
 * Created by rebeccastecker on 6/11/17.
 */

@Database(entities = {Player.class, Game.class, Fact.class, Option.class, GoodMove.class, BadMove.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlayerDao playerDao();
    public abstract GameDao gameDao();
    public abstract FactDao factDao();
    public abstract OptionDao optionsDao();
    public abstract GoodMoveDao goodMovesDao();
    public abstract BadMoveDao badMovesDao();

    private static AppDatabase mInstance;


    public static AppDatabase getDatabase(Context context) {
        if (mInstance == null) {
            mInstance = Room
                    .databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class, "q_color_db"
                    )
                    .build();
        }
        return mInstance;
    }

}