package com.example.myapplication.bluetooth;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class QCGameMessage {

    @JsonCreator
    @ParcelFactory
    public static QCGameMessage build(
            @JsonProperty("action") Action action,
            @JsonProperty("state") GameState state,
            @JsonProperty("set_name") String setName,
            @JsonProperty("fact_count") Integer factCount,
            @JsonProperty("members") List<QCMember> members,
            @JsonProperty("question") String question,
            @JsonProperty("provided_answer") String providedAnswer,
            @JsonProperty("provided_color") String providedColor,
            @JsonProperty("answerer_color") String answererColor,
            @JsonProperty("correct_answer") String correctAnswer,
            @JsonProperty("answered_question") String answeredQuestion
    ) {
        return ImmutableQCGameMessage.builder()
                .action(action)
                .state(state)
                .setName(setName)
                .factCount(factCount)
                .members(members)
                .question(question)
                .providedAnswer(providedAnswer)
                .providedColor(providedColor)
                .answererColor(answererColor)
                .correctAnswer(correctAnswer)
                .answeredQuestion(answeredQuestion)
                .build();
    }

    public enum GameType {
        INFINITE,
        FIRST_PLAYER_TO_POINTS,
        ALL_PLAYERS_TO_POINTS,
        TIMED_GAME
    }

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

    @JsonProperty("action")
    public abstract Action action();

    @JsonProperty("state")
    public abstract GameState state();

    @JsonProperty("members")
    public abstract List<QCMember> members();

    @JsonProperty("type")
    @Nullable
    public abstract GameType gameType();

    @JsonProperty("target")
    @Nullable
    public abstract Integer gameTarget();

    // region Lobby welcome info

    @JsonProperty("set_name")
    @Nullable
    public abstract String setName();

    @JsonProperty("fact_count")
    @Nullable
    public abstract Integer factCount();

    // endregion

    // region default/good user action responses

    /**
     * this is always the "correct" question to the offered answer
     */
    @JsonProperty("question")
    @Nullable
    public abstract String question();

    @JsonProperty("question_color")
    @Nullable
    public abstract String questionColor();

    /**
     * this is always the PROVIDED answer, which may or may not be correct
     */
    @JsonProperty("provided_answer")
    @Nullable
    public abstract String providedAnswer();

    /**
     * this is the color the user TRIED to submit to (regardless if correct or not)
     */
    @JsonProperty("provided_color")
    @Nullable
    public abstract String providedColor();

    @JsonProperty("answerer_color")
    @Nullable
    public abstract String answererColor();
    // endregion

    // region Users messed up submitting an answer, here's the right stuff

    /**
     * this is the "correct" answer (null in the event the user got it right)
     */
    @JsonProperty("correct_answer")
    @Nullable
    public abstract String correctAnswer();

    @JsonProperty("correct_answer_color")
    @Nullable
    public abstract String correctAnswerColor();

    /**
     * this is the question to the answer the user provided (null in the event the user got it right, see {@link #question()})
     */
    @JsonProperty("answered_question")
    @Nullable
    public abstract String answeredQuestion();

    @JsonProperty("answered_question_color")
    @Nullable
    public abstract String answeredQuestionColor();
    // endregion

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
        int currentScore = member.score() == null ? 0 : member.score();
        newMemberList.addAll(members());
        newMemberList.set(index,
                ImmutableQCMember.builder()
                        .from(member)
                        .reaction(reaction)
                        .score(currentScore + reaction.getScoreImpact())
                        .build()
        );
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
                && correctAnswer() == null && answeredQuestion() == null) {
            return this;
        }
        return ImmutableQCGameMessage.builder()
                .from(this)
                .providedAnswer(answer)
                .question(question)
                .correctAnswer(null)
                .answeredQuestion(null)
                .build();
    }

    public QCGameMessage setGameDetails(@NonNull GameType gameType, @Nullable Integer gameTarget) {
        return ImmutableQCGameMessage.builder()
                .from(this)
                .gameTarget(gameTarget)
                .gameType(gameType)
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
    public QCGameMessage setInfoForBadMove(@NonNull String answer,
                                           @NonNull String question,
                                           @NonNull String correctAnswer,
                                           @NonNull String correctQuestion,
                                           @Nullable String askerColor,
                                           @Nullable String answererColor,
                                           @NonNull List<QCMember> askersOfSubmittedQuestion) {

        Set<String> correctColors = new HashSet<>();
        for (QCMember member : askersOfSubmittedQuestion) {
            if (member.color() != null) {
                correctColors.add(member.color());
            }
        }
        if (answer.equals(providedAnswer()) && question.equals(question())
                && correctAnswer.equals(correctAnswer()) && correctQuestion.equals(answeredQuestion())) {
            return this;
        }
        return ImmutableQCGameMessage.builder()
                .from(this)
                .providedAnswer(answer)
                .question(question)
                .correctAnswer(correctAnswer)
                .answeredQuestion(correctQuestion)
                .answererColor(answererColor)
                .providedColor(askerColor)
                .build();

    }

}
