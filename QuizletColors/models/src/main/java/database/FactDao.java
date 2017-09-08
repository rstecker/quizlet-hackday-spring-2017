package database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ui.Fact;

/**
 * Created by rebeccastecker on 6/11/17.
 */

@Dao
public interface FactDao {
    @Query("DELETE FROM fact")
    public void clearGame();

    @Query("SELECT * FROM fact")
    LiveData<List<Fact>> getFacts();

    @Query("SELECT * FROM fact WHERE q_set_id = :setId")
    LiveData<List<Fact>> getFactsForSet(long setId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Fact... facts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Fact> facts);

    @Delete
    void delete(Fact fact);

    @Update
    void update(Fact... facts);

    @Update
    void update(List<Fact> facts);

}
