package sixarmstudios.quizletcolors.logic.engine;

import android.support.annotation.NonNull;

import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCPlayerMessage;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public interface ILobbyLogic {
    public QCGameMessage processMessage(@NonNull QCPlayerMessage message);
}
