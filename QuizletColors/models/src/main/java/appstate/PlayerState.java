package appstate;

/**
 * Created by rebeccastecker on 6/11/17.
 */
public enum PlayerState {
    UNKNOWN_INIT,
    UNKNOWN_STARTING,
    ATTEMPT_OAUTH,
    FIND_SET,
    FIND_GAME,
    LOBBY,
    PLAYING,
    GAME_OVER;

    public String toDBVal() {
        return this.name();
    }
    public static PlayerState fromDBVal(String val) {
        return PlayerState.valueOf(val);
    }
}
