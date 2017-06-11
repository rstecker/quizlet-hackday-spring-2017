package database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ui.Player;

/**
 * Created by rebeccastecker on 6/11/17.
 */

@Dao
public interface PlayerDao {
    @Query("DELETE FROM player")
    public void clearGame();

    @Query("SELECT * FROM player")
    LiveData<List<Player>> getAll();

//    @Query("SELECT * FROM player WHERE uid IN (:userIds)")
//    List<Player> loadAllByIds(int[] userIds);

//    @Query("SELECT * FROM player WHERE username LIKE :first AND "
//            + "last_name LIKE :last LIMIT 1")

    @Query("SELECT * FROM player WHERE username LIKE :username LIMIT 1")
    Player findByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Player... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Player> users);

    @Delete
    void delete(Player user);

    @Update
    void update(Player... users);

    @Update
    void update(List<Player> users);
}