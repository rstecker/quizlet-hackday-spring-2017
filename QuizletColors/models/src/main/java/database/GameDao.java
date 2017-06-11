package database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ui.Game;

/**
 * Created by rebeccastecker on 6/11/17.
 */

@Dao
public interface GameDao {
    @Query("DELETE FROM player")
    public void clearGame();

    @Query("SELECT * FROM game LIMIT 1")
    LiveData<List<Game>> getGame();

    @Query("UPDATE game SET game_state = :state")
    void setGameState(String state);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Game... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Game> users);

    @Delete
    void delete(Game user);

    @Update
    void update(Game... users);

    @Update
    void update(List<Game> users);
}
