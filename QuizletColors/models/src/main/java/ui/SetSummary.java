package ui;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import quizlet.QSet;

/**
 * See {@link quizlet.QSet} for model as it comes off the server
 */
@Entity
public class SetSummary {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public long id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "lang_words")
    public String langWords;

    @ColumnInfo(name = "lang_defs")
    public String langDefs;

    @ColumnInfo(name = "creator_username")
    public String creatorUsername;

    @ColumnInfo(name = "term_count")
    public int termCount;

    @ColumnInfo(name = "last_sync")
    public long lastSynced;

    @ColumnInfo(name = "last_qused")
    public long lastQuizletUse;

    @ColumnInfo(name = "has_images")
    public boolean hasImages;

    public SetSummary() {}

    public SetSummary(QSet qset) {
        this.id = qset.id();
        this.title = qset.title();
        this.description = qset.description();
        this.langDefs = qset.definitionLanguageCode();
        this.langWords = qset.wordLanguageCode();
        this.creatorUsername = qset.creatorUsername();
        this.termCount = qset.termCount();
        this.lastSynced = 0;
        this.lastQuizletUse = qset.modifiedDate();
        this.hasImages = qset.hasImages();
    }
}
