package sixarmstudios.quizletcolors.logic.engine;


import android.support.annotation.NonNull;

import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCMember;
import com.example.myapplication.bluetooth.QCMove;
import com.example.myapplication.bluetooth.QCPlayerMessage;

import java.util.List;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public class PlayLogic implements IPlayLogic {
    private IGameEngine mEngine;

    PlayLogic(IGameEngine gameEngine) {
        mEngine = gameEngine;
    }

    @NonNull @Override
    public QCGameMessage startGame(QCGameMessage.GameType gameType, Integer gameTarget) {
        mEngine.allocateContent();
        return mEngine.generateBasePlayMessage(QCGameMessage.Action.GAME_UPDATE)
                .setGameDetails(gameType, gameTarget);
    }

    @Override public @NonNull QCGameMessage processMessage(@NonNull QCPlayerMessage message) {
        switch (message.action()) {
            case QUERY_GAME:
                return mEngine.generateBasePlayMessage(QCGameMessage.Action.GAME_UPDATE);
            case PLAYER_MOVE:
                QCMove move = message.move();
                String username = message.username();
                if (move == null || username == null) {
                    return mEngine.generateBasePlayMessage(QCGameMessage.Action.GAME_UPDATE);
                }
                QCMember player = mEngine.findMemberByUsername(username);
                QCMember asker = mEngine.findMemberByColor(move.color());
                if (asker == null || player == null) {
                    return mEngine.generateBasePlayMessage(QCGameMessage.Action.GAME_UPDATE);
                }
                return handleMove(player, asker, move.answer());

            case LEAVE_GAME:
                // TODO : actually handle this
                return mEngine.generateBasePlayMessage(QCGameMessage.Action.INVALID_STATE);
            case JOIN_GAME:
                return mEngine.generateBasePlayMessage(QCGameMessage.Action.INVALID_STATE);
            default:
                throw new UnsupportedOperationException("Unable to handle : " + message.action());
        }
    }

    private @NonNull
    QCGameMessage handleMove(@NonNull QCMember player, @NonNull QCMember asker, @NonNull String answer) {
        String answeredQuestion = mEngine.getQuestionForAnswer(answer);
        String correctAnswer = mEngine.getAnswerForPlayer(asker);
        if (correctAnswer.equals(answer)) {
            return handleCorrectMove(player, asker, answer, answeredQuestion);
        } else {
            return handleBadMove(player, asker, answer, answeredQuestion, correctAnswer);
        }
    }

    private @NonNull
    QCGameMessage handleCorrectMove(@NonNull QCMember player, @NonNull QCMember asker, @NonNull String answer, @NonNull String question) {
        mEngine.updatePlayersForCorrectStatus(asker, player, answer);
        return mEngine.generateBasePlayMessage(QCGameMessage.Action.CORRECT_ANSWER)
                .setCorrectInfo(answer, question, mEngine.askersLookingForAnswer(answer))
                .setReactionPlayer(mEngine.findMemberByUsername(asker.username()), QCMember.Reaction.CORRECT_ANSWER_RECEIVED)
                .setReactionPlayer(mEngine.findMemberByUsername(player.username()), QCMember.Reaction.CORRECT_ANSWER_PROVIDED)
                ;
    }

    private @NonNull
    QCGameMessage handleBadMove(@NonNull QCMember player, @NonNull QCMember asker, @NonNull String providedAnswer, @NonNull String answeredQuestion, @NonNull String correctAnswer) {
        String correctQuestion = mEngine.getQuestionForAnswer(correctAnswer);

        // remember : we can only set 1 reaction but someone may BOTH failed to answer AND been an asker. Prioritize correctly
        List<QCMember> othersAtFault = mEngine.getPlayersWithAnswer(correctAnswer);
        List<QCMember> askersOfAnswer = mEngine.askersLookingForAnswer(providedAnswer);

        mEngine.updatePlayersForBadMove(asker, player, providedAnswer, correctAnswer, othersAtFault, askersOfAnswer);

        QCGameMessage.Action action = askersOfAnswer.size() > 0 ? QCGameMessage.Action.WRONG_USER : QCGameMessage.Action.BAD_ANSWER;
        QCGameMessage msg = mEngine.generateBasePlayMessage(action)
                .setInfoForBadMove(providedAnswer, answeredQuestion, correctAnswer, correctQuestion, asker.color(), player.color(), askersOfAnswer);

        for (QCMember member : othersAtFault) {
            msg = msg.setReactionPlayer(mEngine.findMemberByUsername(member.username()), QCMember.Reaction.FAILED_TO_ANSWER);
        }
        for (QCMember member : askersOfAnswer) {
            msg = msg.setReactionPlayer(mEngine.findMemberByUsername(member.username()), QCMember.Reaction.FAILED_TO_RECEIVE_ANSWER);
        }
        // these have to come AFTER the loops above because these reactions "trump" the others
        msg = msg.setReactionPlayer(mEngine.findMemberByUsername(asker.username()), QCMember.Reaction.RECEIVED_BAD_ANSWER);
        msg = msg.setReactionPlayer(mEngine.findMemberByUsername(player.username()), askersOfAnswer.size() > 0 ? QCMember.Reaction.WRONG_USER : QCMember.Reaction.WRONG_CHOICE);
        return msg;
    }

}
