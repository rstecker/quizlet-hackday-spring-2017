package database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ui.GoodMove;
import ui.Option;
import ui.SetSummary;

/**
 * Created by rebeccastecker on 9/7/17.
 */
@Dao
public interface SetSummaryDao {
    @Query("DELETE FROM setSummary")
    public void clearSets();

    @Query("SELECT * FROM setSummary")
    LiveData<List<SetSummary>> getAll();

    @Query("SELECT * FROM setSummary ORDER BY last_sync DESC, last_qused DESC")
    LiveData<List<SetSummary>> getAllByQuizletUse();

    @Query("SELECT * FROM setSummary WHERE id = :setId LIMIT 1")
    LiveData<List<SetSummary>> getSetSummary(long setId);

    @Query("UPDATE setSummary SET last_sync = :time WHERE id = :setId")
    void updateSyncTimestamp(long setId, long time);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(SetSummary... sets);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<SetSummary> sets);

    @Delete
    void delete(SetSummary set);

    @Update
    void update(SetSummary... sets);

    @Update
    void update(List<SetSummary> sets);
}
