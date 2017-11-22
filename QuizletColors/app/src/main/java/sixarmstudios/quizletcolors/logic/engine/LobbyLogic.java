package sixarmstudios.quizletcolors.logic.engine;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.myapplication.bluetooth.ImmutableQCMember;
import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCMember;
import com.example.myapplication.bluetooth.QCPlayerMessage;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import studioes.arm.six.partskit.CompassRose;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public class LobbyLogic implements ILobbyLogic {
    private IGameEngine mEngine;
    public static final String TAG = LobbyLogic.class.getSimpleName();

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
                mEngine.addMember(generateNewMember(message.username(), mEngine.getMemberCount() == 0));
                return mEngine.generateBaseLobbyMessage(QCGameMessage.Action.ACCEPT_REQUEST);

            case PLAYER_MOVE:
                return mEngine.generateBaseLobbyMessage(QCGameMessage.Action.INVALID_STATE);
            default:
                throw new UnsupportedOperationException("Unable to lobby logic handle : " + message.action());
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

    private QCMember generateNewMember(String username, boolean isHost) {
        List<CompassRose.RoseColor> colors = new ArrayList<>();
        colors.addAll(Arrays.asList(CompassRose.RoseColor.values()));
        Collections.shuffle(colors);
        String color = colors.remove(0).colorName();
        while(mEngine.findMemberByColor(color) != null) {
            Log.i(TAG, "Color conflict. There is already a "+color+", trying again");
            color = colors.remove(0).colorName();
        }
        return ImmutableQCMember.builder()
                .username(username)
                .isHost(isHost)
                .color(color)
                .build();
    }
}
