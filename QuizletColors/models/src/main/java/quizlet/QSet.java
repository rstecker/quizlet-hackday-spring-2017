package quizlet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value;

import java.util.List;

import io.reactivex.annotations.Nullable;

/**
 * See {@link ui.SetSummary} for how we store it in the DB
 */
@Value.Immutable
@Value.Style(builder = "new") // builder has to have constructor
@JsonDeserialize(builder = ImmutableQSet.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface QSet {
    @JsonProperty("id") long id();
    @JsonProperty("url") String url();
    @JsonProperty("title") String title();
    @JsonProperty("description") String description();

    @JsonProperty("lang_terms") String wordLanguageCode();
    @JsonProperty("lang_definitions") String definitionLanguageCode();

    @JsonProperty("created_by") String creatorUsername();
    @JsonProperty("creator_id") long creatorId();
    @Nullable @JsonProperty("creator") QUser creator();

    @JsonProperty("term_count") int termCount();
    @JsonProperty("has_images") boolean hasImages();
    @Nullable @JsonProperty("terms") List<QTerm> terms();
    @Nullable @JsonProperty("modified_date") Long modifiedDate();
}
