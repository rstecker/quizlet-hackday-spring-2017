package sixarmstudios.quizletcolors.logic;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.example.myapplication.bluetooth.ImmutableQCMember;
import com.example.myapplication.bluetooth.QCMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public class DistributionLogic implements IDistributionLogic {
    @NonNull @Override
    public List<QCMember> allocateContent(int handSize, @NonNull List<QCMember> members,
                                          @NonNull List<Pair<String, String>> content) {
        List<Pair<String, String>> possibleAnswers = new ArrayList<>();

        List<Pair<String, String>> stuffNotOnTheBoard = new ArrayList<>(content);
        Collections.shuffle(new ArrayList<>(stuffNotOnTheBoard));

        Map<QCMember, List<String>> memberHands = new HashMap<>();
        for (QCMember member : members) {
            memberHands.put(member, new ArrayList<>());
        }

        for (int i = 0; i < handSize; ++i) {
            for (QCMember member : members) {
                if (stuffNotOnTheBoard.size() < 1) {
                    stuffNotOnTheBoard = new ArrayList<>(content);
                    Collections.shuffle(new ArrayList<>(stuffNotOnTheBoard));
                }
                Pair<String, String> entry = stuffNotOnTheBoard.remove(stuffNotOnTheBoard.size() - 1);
                memberHands.get(member).add(entry.second);
                possibleAnswers.add(entry);
            }
        }

        Collections.shuffle(possibleAnswers);
        List<QCMember> result = new ArrayList<>();
        for (QCMember member : members) {
            Pair<String, String> entry = possibleAnswers.remove(0);
            result.add(ImmutableQCMember.builder()
                    .from(member)
                    .options(memberHands.get(member))
                    .question(entry.first)
                    .build());
        }
        return result;
    }

    @NonNull @Override
    public List<QCMember> updateForCorrectMove(@NonNull QCMember asker,
                                               @NonNull QCMember answerer,
                                               @NonNull String answer,
                                               @NonNull List<QCMember> members,
                                               @NonNull List<Pair<String, String>> content) {
        Map<String, Pair<String, String>> answerLookup = new HashMap<>();
        Map<String, Pair<String, String>> questionLookup = new HashMap<>();
        populateContentLists(content, answerLookup, questionLookup);

        List<Pair<String, String>> possibleAnswers = new ArrayList<>();
        List<Pair<String, String>> stuffNotOnTheBoard = new ArrayList<>(content);
        Map<QCMember, List<String>> memberHands = new HashMap<>();
        Map<QCMember, Pair<String, String>> memberQuestions = new HashMap<>();
        populateCurrentStateLists(members, answerLookup, questionLookup, stuffNotOnTheBoard,
                possibleAnswers, memberHands, memberQuestions);

        selectNewQuestion(possibleAnswers, answerLookup, answer, memberQuestions, asker);
        selectNewOption(stuffNotOnTheBoard, content, memberHands.get(answerer), answer);

        return buildResultList(members, memberHands, memberQuestions);
    }


    @NonNull @Override
    public List<QCMember> updateForBadMove(@NonNull QCMember asker,
                                           @NonNull QCMember answerer,
                                           @NonNull String providedAnswer,
                                           @NonNull String correctAnswer,
                                           @NonNull List<QCMember> othersAtFault,
                                           @NonNull List<QCMember> askersOfAnswer,
                                           @NonNull List<QCMember> members,
                                           @NonNull List<Pair<String, String>> content) {

        Map<String, Pair<String, String>> answerLookup = new HashMap<>();
        Map<String, Pair<String, String>> questionLookup = new HashMap<>();
        populateContentLists(content, answerLookup, questionLookup);

        List<Pair<String, String>> possibleAnswers = new ArrayList<>();
        List<Pair<String, String>> stuffNotOnTheBoard = new ArrayList<>(content);
        Map<QCMember, List<String>> memberHands = new HashMap<>();
        Map<QCMember, Pair<String, String>> memberQuestions = new HashMap<>();
        populateCurrentStateLists(members, answerLookup, questionLookup, stuffNotOnTheBoard,
                possibleAnswers, memberHands, memberQuestions);

        selectNewQuestion(possibleAnswers, answerLookup, correctAnswer, memberQuestions, asker);
        for (QCMember member : askersOfAnswer) {
            selectNewQuestion(possibleAnswers, answerLookup, providedAnswer, memberQuestions, member);
        }
        selectNewOption(stuffNotOnTheBoard, content, memberHands.get(answerer), providedAnswer);
        for (QCMember member : othersAtFault) {
            selectNewOption(stuffNotOnTheBoard, content, memberHands.get(member), correctAnswer);
        }

        return buildResultList(members, memberHands, memberQuestions);
    }

    /**
     * Grabs a new question, updates list
     *
     * @param possibleAnswers not modified
     * @param answerLookup    not modified
     * @param memberQuestions should be populated, will be updated for provided member
     */
    private void selectNewQuestion(@NonNull List<Pair<String, String>> possibleAnswers,
                                   @NonNull Map<String, Pair<String, String>> answerLookup,
                                   @NonNull String oldAnswer,
                                   @NonNull Map<QCMember, Pair<String, String>> memberQuestions,
                                   @NonNull QCMember member
    ) {
        List<Pair<String, String>> answersToWorkWith = new ArrayList<>(possibleAnswers);
        answersToWorkWith.remove(answerLookup.get(oldAnswer));       // lets not ask the question we just did!
        List<Pair<String, String>> unaskedPossible = new ArrayList<>(answersToWorkWith);
        unaskedPossible.removeAll(memberQuestions.values());    // lets not ask the same thing as someone else
        if (unaskedPossible.size() > 0) {
            Collections.shuffle(new ArrayList<>(unaskedPossible));
            memberQuestions.put(member, unaskedPossible.remove(0));    // shuffle and grab the first possible question
        } else {
            Collections.shuffle(new ArrayList<>(answersToWorkWith));  // oh no, the only possible answers are the asked ones. whatever
            memberQuestions.put(member, answersToWorkWith.remove(0));    // shuffle and grab the first possible question
        }
    }

    /**
     * Grabs a new answer, updates lists
     *
     * @param stuffNotOnTheBoard will be modified (shuffled, items removed)
     * @param allContent         will not be modified
     * @param optionsForMember   will be modified
     */
    private void selectNewOption(@NonNull List<Pair<String, String>> stuffNotOnTheBoard,
                                 @NonNull List<Pair<String, String>> allContent,
                                 @NonNull List<String> optionsForMember,
                                 @NonNull String oldAnswer) {
        // TODO : be aware we could be stranding a user with the same question as the 'asker' and no longer someone has the answer
        String newAnswer = null;
        if (stuffNotOnTheBoard.size() > 0) {    // ideally we want to add NEW stuff to the board
            Collections.shuffle(stuffNotOnTheBoard);
            newAnswer = stuffNotOnTheBoard.remove(0).second;
        } else {    // if everything is already on the board, just grab something random for now
            List<Pair<String, String>> tmpContent = new ArrayList<>(allContent);
            Collections.shuffle(tmpContent);
            Pair<String, String> newOption = tmpContent.remove(0);
            stuffNotOnTheBoard.remove(newOption);
            newAnswer = newOption.second;
        }
        optionsForMember.remove(oldAnswer);
        optionsForMember.add(newAnswer);
    }

    /**
     * @param members            not modified
     * @param answerLookup       not modified
     * @param questionLookup     not modified
     * @param stuffNotOnTheBoard should be full list, things will be removed from it
     * @param possibleAnswers    should be empty, will be populated
     * @param memberHands        should be empty, will be populated
     * @param memberQuestions    should be empty, will be populated
     */
    private void populateCurrentStateLists(@NonNull List<QCMember> members,
                                           @NonNull Map<String, Pair<String, String>> answerLookup,
                                           @NonNull Map<String, Pair<String, String>> questionLookup,
                                           @NonNull List<Pair<String, String>> stuffNotOnTheBoard,
                                           @NonNull List<Pair<String, String>> possibleAnswers,
                                           @NonNull Map<QCMember, List<String>> memberHands,
                                           @NonNull Map<QCMember, Pair<String, String>> memberQuestions) {
        for (QCMember member : members) {
            List<String> options = member.options();
            String question = member.question();
            if (options == null || question == null) {
                continue;
            }
            memberHands.put(member, new ArrayList<>(options));
            memberQuestions.put(member, questionLookup.get(question));
            for (String option : options) {
                stuffNotOnTheBoard.remove(answerLookup.get(option));
                possibleAnswers.add(answerLookup.get(option));
            }
        }
    }

    /**
     * @param allContent     not modified
     * @param answerLookup   should be empty, will be populated
     * @param questionLookup should be empty, will be populated
     */
    private void populateContentLists(@NonNull List<Pair<String, String>> allContent,
                                      @NonNull Map<String, Pair<String, String>> answerLookup,
                                      @NonNull Map<String, Pair<String, String>> questionLookup) {
        for (Pair<String, String> pair : allContent) {
            questionLookup.put(pair.first, pair);
            answerLookup.put(pair.second, pair);
        }
    }

    private List<QCMember> buildResultList(@NonNull List<QCMember> members,
                                           @NonNull Map<QCMember, List<String>> memberHands,
                                           @NonNull Map<QCMember, Pair<String, String>> memberQuestions) {
        List<QCMember> result = new ArrayList<>();
        for (QCMember member : members) {
            result.add(ImmutableQCMember.builder()
                    .from(member)
                    .options(memberHands.get(member))
                    .question(memberQuestions.get(member).first)
                    .build());
        }
        return result;
    }
}
