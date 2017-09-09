package database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import appstate.AppState;
import appstate.PlayerState;

/**
 * Created by rebeccastecker on 9/7/17.
 */
@Dao
public interface ApplicationStateDao {
    @Query("DELETE FROM appState")
    public void clearState();

    @Query("SELECT * FROM appState LIMIT 1")
    LiveData<List<AppState>> getGame();

    @Query("UPDATE appState SET playState = :playerState")
    void updatePlayerState(String playerState);

    @Query("UPDATE appState SET playState = :playerState, qusername = :username, qtoken = :token")
    void updatePlayerStateWithQuizletAuth(String playerState, String token, String username);

    @Query("UPDATE appState SET q_set_id = :setId")
    void updateCurrentSetId(long setId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(AppState... games);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AppState> games);

    @Delete
    void delete(AppState game);

    @Update
    void update(AppState... games);

    @Update
    void update(List<AppState> games);

}
