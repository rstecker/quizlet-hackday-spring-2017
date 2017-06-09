package sixarmstudios.quizletcolors.logic.engine;

import android.support.annotation.NonNull;

import com.example.myapplication.bluetooth.ImmutableQCMember;
import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCMember;
import com.example.myapplication.bluetooth.QCPlayerMessage;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public class LobbyLogic implements ILobbyLogic {
    private IGameEngine mEngine;

     LobbyLogic(IGameEngine gameEngine) {
        mEngine = gameEngine;
    }

    @Override public QCGameMessage processMessage(@NonNull QCPlayerMessage message) {
        switch (message.action()) {
            case QUERY_GAME:
                return mEngine.generateBaseLobbyMessage(QCGameMessage.Action.LOBBY_WELCOME);
            case LEAVE_GAME:
                handleLeaveGame(message);
                return mEngine.generateBaseLobbyMessage(QCGameMessage.Action.LOBBY_UPDATE);
            case JOIN_GAME:
                mEngine.addMember(generateNewMember(message.username()));
                return mEngine.generateBaseLobbyMessage(QCGameMessage.Action.ACCEPT_REQUEST);

            case PLAYER_MOVE:
                return mEngine.generateBaseLobbyMessage(QCGameMessage.Action.INVALID_STATE);
            default:
                throw new UnsupportedOperationException("Unable to handle : " + message.action());
        }
    }

    private void handleLeaveGame(@NonNull QCPlayerMessage message) {
        String username = message.username();
        if (StringUtils.isBlank(username)) {
            return;
        }
        QCMember member = mEngine.findMemberByUsername(username);
        if (member == null) {
            return;
        }
        mEngine.removeMember(member);
    }

    private QCMember generateNewMember(String username) {
        return ImmutableQCMember.builder()
                .username(username)
                .build();
    }
}
