package sixarmstudios.quizletcolors.logic.engine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

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
    public static final String TAG = DistributionLogic.class.getSimpleName();

    @NonNull
    @Override
    public List<QCMember> allocateContent(int handSize, @NonNull List<QCMember> members,
                                          @NonNull List<Pair<String, String>> content) {
        List<Pair<String, String>> possibleAnswers = new ArrayList<>();

        List<Pair<String, String>> stuffNotOnTheBoard = new ArrayList<>(content);
        Collections.shuffle(stuffNotOnTheBoard);

        Map<QCMember, List<String>> memberHands = new HashMap<>();
        for (QCMember member : members) {
            memberHands.put(member, new ArrayList<>());
        }

        for (int i = 0; i < handSize; ++i) {
            for (QCMember member : members) {
                if (stuffNotOnTheBoard.size() < 1) {
                    stuffNotOnTheBoard = new ArrayList<>(content);
                    Collections.shuffle(stuffNotOnTheBoard);
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

    @NonNull
    @Override
    public List<QCMember> updateForCorrectMove(@NonNull QCMember asker,
                                               @NonNull QCMember answerer,
                                               @NonNull String answer,
                                               @NonNull List<QCMember> members,
                                               @NonNull List<Pair<String, String>> content) {
        Log.d(TAG, "Re allocating content in response to a good move");
        Map<String, Pair<String, String>> answerLookup = new HashMap<>();
        Map<String, Pair<String, String>> questionLookup = new HashMap<>();
        populateContentLists(content, answerLookup, questionLookup);

        List<Pair<String, String>> possibleAnswers = new ArrayList<>();
        List<Pair<String, String>> stuffNotOnTheBoard = new ArrayList<>(content);
        Map<QCMember, List<String>> memberHands = new HashMap<>();
        Map<QCMember, Pair<String, String>> memberQuestions = new HashMap<>();
        populateCurrentStateLists(members, answerLookup, questionLookup, stuffNotOnTheBoard,
                possibleAnswers, memberHands, memberQuestions);

        Log.d(TAG, "Member " + asker.username() + " had question " + memberQuestions.get(asker).first);
        selectNewQuestion(possibleAnswers, answerLookup, answer, memberQuestions, memberHands, asker);
        Log.d(TAG, "Member " + asker.username() + " just got question " + memberQuestions.get(asker).first);

        Log.d(TAG, "Member " + answerer.username() + " had options " + memberHands.get(answerer));
        selectNewOption(stuffNotOnTheBoard, content, memberHands.get(answerer), answer);
        Log.d(TAG, "Member " + answerer.username() + " just got options " + memberHands.get(answerer));

        return buildResultList(members, memberHands, memberQuestions, answerer);
    }


    @NonNull
    @Override
    public List<QCMember> updateForBadMove(@NonNull QCMember asker,
                                           @NonNull QCMember answerer,
                                           @NonNull String providedAnswer,
                                           @NonNull String correctAnswer,
                                           @NonNull List<QCMember> othersAtFault,
                                           @NonNull List<QCMember> askersOfAnswer,
                                           @NonNull List<QCMember> members,
                                           @NonNull List<Pair<String, String>> content) {

        System.out.println("Re allocating content in response to a bad move [provided '" + providedAnswer + "'] [correct '" + correctAnswer + "']");
        Map<String, Pair<String, String>> answerLookup = new HashMap<>(); // key is an answer, result is the pair
        Map<String, Pair<String, String>> questionLookup = new HashMap<>(); // key is a question, result is the pair
        populateContentLists(content, answerLookup, questionLookup);

        List<Pair<String, String>> possibleAnswers = new ArrayList<>();
        List<Pair<String, String>> stuffNotOnTheBoard = new ArrayList<>(content);
        Map<QCMember, List<String>> memberHands = new HashMap<>();
        Map<QCMember, Pair<String, String>> memberQuestions = new HashMap<>();
        populateCurrentStateLists(members, answerLookup, questionLookup, stuffNotOnTheBoard,
                possibleAnswers, memberHands, memberQuestions);

        possibleAnswers.remove(answerLookup.get(correctAnswer));
        possibleAnswers.remove(answerLookup.get(providedAnswer));  // gotta' make sure we don't ask the answers we're removing from the board
        System.out.println("Removing from possible answers [" + answerLookup.get(providedAnswer) + "] & [" + answerLookup.get(correctAnswer) + "], remaining :" + possibleAnswers.size());

        System.out.println("Answer re-asignment - answered question was '" + answerLookup.get(providedAnswer) + "', askers were '" + askersOfAnswer + "'");
        System.out.println("Member '" + asker.username() + "' had question " + memberQuestions.get(asker).first);
        selectNewQuestion(possibleAnswers, answerLookup, correctAnswer, memberQuestions, memberHands, asker);
        System.out.println("Member '" + asker.username() + "' just got question " + memberQuestions.get(asker).first);
        for (QCMember member : askersOfAnswer) {
            System.out.println("Member '" + member.username() + "' had question " + memberQuestions.get(member).first);
            selectNewQuestion(possibleAnswers, answerLookup, providedAnswer, memberQuestions, memberHands, member);
            System.out.println("Member '" + member.username() + "' just got question " + memberQuestions.get(member).first);
        }
        System.out.println("Provided answer was " + providedAnswer + ", correct answer was " + correctAnswer);
        System.out.println("Member " + answerer.username() + " had options " + memberHands.get(answerer));
        selectNewOption(stuffNotOnTheBoard, content, memberHands.get(answerer), providedAnswer);
        System.out.println("Member " + answerer.username() + " just got options " + memberHands.get(answerer));
        for (QCMember member : othersAtFault) {
            System.out.println("Member '" + member.username() + "' had options " + memberHands.get(member));
            if (member.equals(answerer)) {
                List<Pair<String, String>> tmpContent = new ArrayList<>(content);
                tmpContent.remove(answerLookup.get(providedAnswer));
                selectNewOption(stuffNotOnTheBoard, tmpContent, memberHands.get(member), correctAnswer);
            } else {
                selectNewOption(stuffNotOnTheBoard, content, memberHands.get(member), correctAnswer);
            }
            System.out.println("Member '" + member.username() + "' just got options " + memberHands.get(member));

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
                                   @NonNull Map<QCMember, List<String>> memberHands,
                                   @NonNull QCMember member
    ) {
        List<Pair<String, String>> answersToWorkWith = new ArrayList<>(possibleAnswers);
        answersToWorkWith.remove(answerLookup.get(oldAnswer));       // lets not ask the question we just did!
        List<Pair<String, String>> unaskedPossible = new ArrayList<>(answersToWorkWith);
        unaskedPossible.removeAll(memberQuestions.values());    // lets not ask the same thing as someone else
//        for (String option : memberHands.get(member)) {
//            unaskedPossible.remove(answerLookup.get(option));   // lets not ask ones that we have ourselves (FOR NOW)
//        }
        if (unaskedPossible.size() > 0) {
            Collections.shuffle(unaskedPossible);
            memberQuestions.put(member, unaskedPossible.remove(0));    // shuffle and grab the first possible question
        } else {
            Collections.shuffle(answersToWorkWith);  // oh no, the only possible answers are the asked ones. whatever
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
    void selectNewOption(@NonNull List<Pair<String, String>> stuffNotOnTheBoard,
                         @NonNull List<Pair<String, String>> allContent,
                         @NonNull List<String> optionsForMember,
                         @NonNull String oldAnswer) {
        if (optionsForMember.indexOf(oldAnswer) == -1) {
            throw new IllegalStateException("Tried to remove " + oldAnswer + " from answer list " + optionsForMember);
        }
        // TODO : be aware we could be stranding a user with the same question as the 'asker' and no longer someone has the answer
        String newAnswer = null;
        if (stuffNotOnTheBoard.size() > 0) {    // ideally we want to add NEW stuff to the board
            Collections.shuffle(stuffNotOnTheBoard);
            newAnswer = stuffNotOnTheBoard.remove(0).second;
            System.out.println("  > new answer (stuff not on the board) : " + newAnswer);
        } else {    // if everything is already on the board, just grab something random for now
            List<Pair<String, String>> tmpContent = new ArrayList<>(allContent);
            for (int i = tmpContent.size() - 1; i >= 0; --i) {
                String answer = tmpContent.get(i).second;
                if (optionsForMember.contains(answer) || answer.equals(oldAnswer)) {
                    System.out.println("  > trying to remove " + answer + " @ position " + i);
                    tmpContent.remove(i);
                }
            }
            Collections.shuffle(tmpContent);
            System.out.println("  > after avoiding old answer '" + oldAnswer + "' and hand: " + optionsForMember + " we pruned down to > " + tmpContent);
            Pair<String, String> newOption = tmpContent.remove(0);
            stuffNotOnTheBoard.remove(newOption);
            newAnswer = newOption.second;
            System.out.println("  > new answer (everything is already on the board) : " + newAnswer);
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
    void populateContentLists(@NonNull List<Pair<String, String>> allContent,
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
        return buildResultList(members, memberHands, memberQuestions, null);
    }

    private List<QCMember> buildResultList(@NonNull List<QCMember> members,
                                           @NonNull Map<QCMember, List<String>> memberHands,
                                           @NonNull Map<QCMember, Pair<String, String>> memberQuestions,
                                           @Nullable QCMember pointScorer) {
        List<QCMember> result = new ArrayList<>();
        for (QCMember member : members) {
            int score = member.score() == null ? 0 : member.score();
            if (member.equals(pointScorer)) {
                ++score;
            }
            result.add(ImmutableQCMember.builder()
                    .from(member)
                    .options(memberHands.get(member))
                    .question(memberQuestions.get(member).first)
                    .score(score)
                    .build());
        }
        return result;
    }
}
