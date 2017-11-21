package sixarmstudios.quizletcolors.logic.engine;

import android.support.annotation.NonNull;

import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCPlayerMessage;

/**
 * Created by rebeccastecker on 11/21/17.
 */

public class EndLogic implements IEndLogic {
    private IGameEngine mEngine;

    public EndLogic(IGameEngine gameEngine) {
        mEngine = gameEngine;
    }

    @Override
    public QCGameMessage processMessage(@NonNull QCPlayerMessage message) {
        switch (message.action()) {
            case QUERY_GAME:
            case PLAYER_MOVE:
            case LEAVE_GAME:
            case JOIN_GAME:
                return mEngine.generateBaseEndGameMessage(QCGameMessage.Action.GAME_OVER);
            default:
                throw new UnsupportedOperationException("Unable to handle : " + message.action());
        }
    }
}
