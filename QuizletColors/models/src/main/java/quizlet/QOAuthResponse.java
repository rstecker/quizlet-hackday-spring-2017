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
@JsonDeserialize(builder = ImmutableQOAuthResponse.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface QOAuthResponse {
    @JsonProperty("access_token")
    String accessToken();

    @JsonProperty("token_type")
    String tokenType();

    @JsonProperty("expires_in")
    int expiresIn();

    @JsonProperty("scope")
    String scope();


    @JsonProperty("user_id")
    String username();
}
