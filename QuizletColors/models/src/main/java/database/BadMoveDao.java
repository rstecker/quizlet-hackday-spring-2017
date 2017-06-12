package database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ui.BadMove;

/**
 * Created by rebeccastecker on 6/12/17.
 */
@Dao
public interface BadMoveDao {
    @Query("DELETE FROM badMove")
    public void clearMoves();

    @Query("SELECT * FROM badMove WHERE you_were_given_bad_answer = 1 OR you_answered_poorly = 1 OR you_failed_to_answer = 1 OR your_answer_went_to_someone_else = 1 ORDER BY timestamp DESC LIMIT 1")
    LiveData<List<BadMove>> getYourMostRecent();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(BadMove... moves);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BadMove> moves);

    @Delete
    void delete(BadMove move);

    @Update
    void update(BadMove... moves);

    @Update
    void update(List<BadMove> moves);
}

