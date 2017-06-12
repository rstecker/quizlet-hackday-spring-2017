package quizlet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value;

/**
 * Created by rebeccastecker on 6/12/17.
 */

@Value.Immutable
@Value.Style(builder = "new") // builder has to have constructor
@JsonDeserialize(builder = ImmutableQUser.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface QUser {
    @JsonProperty("id") int id();
    @JsonProperty("username") String username();
    @JsonProperty("account_type") String accountType();
    @JsonProperty("profile_image") String profileImageUrl();
}