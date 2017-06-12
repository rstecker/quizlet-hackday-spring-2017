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

/**
 * Created by rebeccastecker on 6/12/17.
 */
@Dao
public interface GoodMoveDao {
    @Query("DELETE FROM goodMove")
    public void clearMoves();

    @Query("SELECT * FROM goodMove WHERE you_asked = 1 OR you_answered = 1 ORDER BY timestamp DESC LIMIT 1")
    LiveData<List<GoodMove>> getYourMostRecent();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(GoodMove... moves);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GoodMove> moves);

    @Delete
    void delete(GoodMove move);

    @Update
    void update(GoodMove... moves);

    @Update
    void update(List<GoodMove> moves);
}
