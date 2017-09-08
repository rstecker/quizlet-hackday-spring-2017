package ui.immutable;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value;

import quizlet.ImmutableQOAuthResponse;
import ui.SetSummary;

/**
 * Created by rebeccastecker on 9/7/17.
 */
@Value.Immutable
@Value.Style(builder = "new") // builder has to have constructor
@JsonDeserialize(builder = ImmutableImmSetSummary.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ImmSetSummary {
    long id();
    public String title();

    public String description();

    public String langWords();

    public String langDefs();

    public String creatorUsername();

    public int termCount();

    public long lastSynced();

    public long lastQuizletUse();

    public boolean hasImages();

    public static ImmSetSummary from(SetSummary summary) {
        return new ImmutableImmSetSummary.Builder()
                .id(summary.id)
                .title(summary.title)
                .description(summary.description)
                .langDefs(summary.langDefs)
                .langWords(summary.langWords)
                .creatorUsername(summary.creatorUsername)
                .termCount(summary.termCount)
                .lastSynced(summary.lastSynced)
                .lastQuizletUse(summary.lastQuizletUse)
                .hasImages(summary.hasImages)
                .build();

    }
}
