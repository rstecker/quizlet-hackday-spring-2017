package gamelogic;

import android.support.annotation.Nullable;

import com.example.myapplication.bluetooth.QCGameMessage;

import org.immutables.value.Value;

import java.util.List;

import ui.Player;

/**
 * Created by rebeccastecker on 11/21/17.
 */

@Value.Immutable
public abstract class  EndState {

    public abstract List<Player> players();

    @Nullable
    public abstract QCGameMessage.GameType gameType();
    @Nullable public abstract Integer gameTarget();
}
