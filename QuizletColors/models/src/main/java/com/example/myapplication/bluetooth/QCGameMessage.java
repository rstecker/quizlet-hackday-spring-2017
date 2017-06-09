package com.example.myapplication.bluetooth;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.immutables.value.Value;
import org.parceler.Parcel;
import org.parceler.ParcelFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This represents all possible messages coming out from the Host/Game Engine and informing Players
 * of the state of the world (and how the world has responded to their submitted {@link QCPlayerMessage}s)
 */
@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableQCGameMessage.class)
@Value.Immutable
public abstract class QCGameMessage {

    public enum Action {
        LOBBY_WELCOME,
        INVALID_STATE,

        REJECT_REQUEST,
        ACCEPT_REQUEST,
        LOBBY_UPDATE,
        LOBBY_KICK,
        LOBBY_CANCEL,

        GAME_UPDATE,
        BAD_ANSWER,
        WRONG_USER,
        CORRECT_ANSWER,
        OUT_OF_SYNC

    }

    public abstract Action action();

    public abstract GameState state();

    /**
     * The "Host" is always the first member in the list
     */
    public abstract List<QCMember> members();

    // region default/good user action responses

    /**
     * this is always the "correct" question to the offered answer
     */
    @Nullable public abstract String question();

    /**
     * this is always the PROVIDED answer, which may or may not be correct
     */
    @Nullable public abstract String providedAnswer();

    /**
     * this is the color the user TRIED to submit to (regardless if correct or not)
     */
    @Nullable public abstract String providedColor();

    @Nullable public abstract String answererColor();
    // endregion

    // region Users messed up submitting an answer, here's the right stuff

    /**
     * this is the "correct" answer (null in the event the user got it right)
     */
    @Nullable public abstract String correctAnswer();

    /**
     * this is the question to the answer the user provided (null in the event the user got it right, see {@link #question()})
     */
    @Nullable public abstract String answeredQuestion();

    /**
     * this is the list of colors who had questions matching the provided answer. May be null if the
     * {@link #action()} doesn't make sense or empty if this is an graded response but no one actually
     * was actively asking the question at the time
     */
    @Nullable public abstract Set<String> correctColors();
    // endregion

    @ParcelFactory
    public static QCGameMessage build(Action action, GameState state) {
        return ImmutableQCGameMessage.builder()
                .action(action)
                .state(state)
                .build();
    }


    public QCGameMessage addMember(QCMember member) {
        int index = members().indexOf(member);
        if (index >= 0) {
            return this;
        }
        List<QCMember> newMemberList = new ArrayList<>();
        newMemberList.addAll(members());
        newMemberList.add(member);
        return ImmutableQCGameMessage.builder()
                .from(this)
                .members(newMemberList)
                .build();
    }

    /**
     * @param member assumed member in the members list. If member cannot be found, no changes are made
     * @return a new {@link QCGameMessage} with updated member reaction
     */
    public @NonNull
    QCGameMessage setReactionPlayer(@Nullable QCMember member, @NonNull QCMember.Reaction reaction) {
        if (member == null) {
            return this;
        }
        int index = members().indexOf(member);
        if (index < 0) {
            return this;
        }
        List<QCMember> newMemberList = new ArrayList<>();
        newMemberList.addAll(members());
        newMemberList.set(index, ImmutableQCMember.builder().from(member).reaction(reaction).build());
        return ImmutableQCGameMessage.builder()
                .from(this)
                .members(newMemberList)
                .build();
    }

    /**
     * This assumes the user got the question right!
     *
     * @param answer   the PROVIDED answer
     * @param question the CORRECT question for the provided answer
     */
    public QCGameMessage setCorrectInfo(@NonNull String answer, @NonNull String question, @NonNull List<QCMember> askersOfSubmittedQuestion) {
        Set<String> correctColors = new HashSet<>();
        for (QCMember member : askersOfSubmittedQuestion) {
            if (member.color() != null) {
                correctColors.add(member.color());
            }
        }
        if (answer.equals(providedAnswer()) && question.equals(question())
                && correctAnswer() == null && answeredQuestion() == null
                && correctColors.equals(correctColors())) {
            return this;
        }
        return ImmutableQCGameMessage.builder()
                .from(this)
                .providedAnswer(answer)
                .question(question)
                .correctAnswer(null)
                .answeredQuestion(null)
                .correctColors(correctColors)
                .build();
    }

    /**
     * This assumes the user got the question WRONG!
     *
     * @param answer          the answer provided by the user
     * @param question        the question that goes with the provided answer
     * @param correctAnswer   the answer for the user for whom the answer was incorrectly submitted to
     * @param correctQuestion the question of the user for whom the answer was incorrectly submitted to
     */
    public QCGameMessage setInfoForBadMove(@NonNull String answer, @NonNull String question,
                                           @NonNull String correctAnswer, @NonNull String correctQuestion,
                                           @NonNull List<QCMember> askersOfSubmittedQuestion) {

        Set<String> correctColors = new HashSet<>();
        for (QCMember member : askersOfSubmittedQuestion) {
            if (member.color() != null) {
                correctColors.add(member.color());
            }
        }
        if (answer.equals(providedAnswer()) && question.equals(question())
                && correctAnswer.equals(correctAnswer()) && correctQuestion.equals(answeredQuestion())
                && correctColors.equals(correctColors())) {
            return this;
        }
        return ImmutableQCGameMessage.builder()
                .from(this)
                .providedAnswer(answer)
                .question(question)
                .correctAnswer(correctAnswer)
                .answeredQuestion(correctQuestion)
                .correctColors(correctColors)
                .build();

    }

}
