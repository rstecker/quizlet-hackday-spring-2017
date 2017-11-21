package sixarmstudios.quizletcolors.logic.engine;

import android.support.annotation.NonNull;

import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCPlayerMessage;

/**
 * Created by rebeccastecker on 11/21/17.
 */

public interface IEndLogic {
    public QCGameMessage processMessage(@NonNull QCPlayerMessage message);
}
