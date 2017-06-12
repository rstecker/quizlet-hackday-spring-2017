package database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ui.Option;

/**
 * Created by rebeccastecker on 6/11/17.
 */

@Dao
public interface OptionDao {
    @Query("DELETE FROM option")
    public void clearGame();

    @Query("SELECT * FROM option")
    LiveData<List<Option>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Option... options);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Option> options);

    @Delete
    void delete(Option option);

    @Update
    void update(Option... options);

    @Update
    void update(List<Option> options);
}
