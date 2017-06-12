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
    @Query("DELETE FROM game")
    public void clearGame();

    @Query("SELECT * FROM game LIMIT 1")
    LiveData<List<Game>> getGame();

    @Query("UPDATE game SET game_state = :state")
    void setGameState(String state);

    @Query("UPDATE game SET question = :question")
    void setCurrentQuestion(String question);

    // TODO : I should probably re-evanuate how I'm storing board answer options.
    @Query("UPDATE game SET answer_option_1 = :opt1, answer_option_2 = :opt2, answer_option_3 = :opt3, answer_option_4 = :opt4")
    void setOptions(String opt1, String opt2, String opt3, String opt4);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Game... games);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Game> games);

    @Delete
    void delete(Game game);

    @Update
    void update(Game... games);

    @Update
    void update(List<Game> games);
}
