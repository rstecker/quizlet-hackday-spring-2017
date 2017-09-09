package quizlet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value;

/**
 * Created by rebeccastecker on 9/7/17.
 */

@Value.Immutable
@Value.Style(builder = "new") // builder has to have constructor
@JsonDeserialize(builder = ImmutableQStudied.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface QStudied {
    @JsonProperty("mode") String mode();
    @JsonProperty("set") QSet set();
    @JsonProperty("finish_date") long finishTime();
}
