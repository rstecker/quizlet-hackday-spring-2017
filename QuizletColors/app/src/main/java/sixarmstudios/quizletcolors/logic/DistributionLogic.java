package sixarmstudios.quizletcolors.logic;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.example.myapplication.bluetooth.ImmutableQCMember;
import com.example.myapplication.bluetooth.QCMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public class DistributionLogic implements IDistributionLogic {
    @NonNull @Override
    public Set<QCMember> allocateContent(int handSize, @NonNull Set<QCMember> members,
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
                Pair<String,String> entry = stuffNotOnTheBoard.remove(stuffNotOnTheBoard.size() - 1);
                memberHands.get(member).add(entry.second);
                possibleAnswers.add(entry);
            }
        }

        Collections.shuffle(possibleAnswers);
        Set<QCMember> result = new HashSet<>();
        for (QCMember member : members) {
            Pair<String,String> entry = possibleAnswers.remove(0);
            result.add(ImmutableQCMember.builder()
                    .from(member)
                    .options(memberHands.get(member))
                    .question(entry.first)
                    .build());
        }
        return result;
    }

    @NonNull @Override
    public Set<QCMember> updateForCorrectMove(@NonNull QCMember asker,
                                              @NonNull QCMember answerer,
                                              @NonNull String answer,
                                              @NonNull List<QCMember> members,
                                              @NonNull List<Pair<String, String>> content) {
        List<Pair<String, String>> possibleAnswers = new ArrayList<>();
        Map<String, Pair<String, String>> answerLookup = new HashMap<>();
        Map<String, Pair<String, String>> questionLookup = new HashMap<>();
        for (Pair<String, String> pair : content) {
            questionLookup.put(pair.first, pair);
            answerLookup.put(pair.second, pair);
        }

        List<Pair<String, String>> stuffNotOnTheBoard = new ArrayList<>(content);
        List<Pair<String, String>> stuffNotBeingAsked = new ArrayList<>(content);

        Map<QCMember, List<String>> memberHands = new HashMap<>();
        Map<QCMember, String> memberQuestions = new HashMap<>();
        for (QCMember member : members) {
            List<String> options = member.options();
            String question = member.question();
            if (options == null || question == null) {
                continue;
            }
            memberHands.put(member, new ArrayList<>(options));
            memberQuestions.put(member, question);
            stuffNotBeingAsked.remove(questionLookup.get(question));
            for (String option : options) {
                stuffNotOnTheBoard.remove(answerLookup.get(option));
                possibleAnswers.add(answerLookup.get(option));
            }
        }
        possibleAnswers.remove(answerLookup.get(answer));   // lets not ask the question we just did!
        
        possibleAnswers.removeAll(memberQuestions.values());
        Collections.shuffle(new ArrayList<>(possibleAnswers));
        memberQuestions.put(asker, possibleAnswers.remove(0).first);    // shuffle and grab the first possible question


        // TODO : be aware we could be stranding a user with the same question as the 'asker' and no longer someone has the answer
        String newAnswer = null;
        if (stuffNotOnTheBoard.size() > 0) {    // ideally we want to add NEW stuff to the board
            Collections.shuffle(new ArrayList<>(stuffNotOnTheBoard));
             newAnswer = stuffNotOnTheBoard.remove(0).second;
        } else {    // if everything is already on the board, just grab something random for now
            Collections.shuffle(new ArrayList<>(content));
             newAnswer = stuffNotOnTheBoard.remove(0).second;
        }
        memberHands.get(answerer).remove(answer);
        memberHands.get(answerer).add(newAnswer);

        Set<QCMember> result = new HashSet<>();
        for (QCMember member : members) {
            result.add(ImmutableQCMember.builder()
                    .from(member)
                    .options(memberHands.get(member))
                    .question(memberQuestions.get(member))
                    .build());
        }
        return result;
    }

    @NonNull @Override
    public Set<QCMember> updateForBadMove(@NonNull QCMember asker,
                                          @NonNull QCMember answerer,
                                          @NonNull String providedAnswer,
                                          @NonNull String correctAnswer,
                                          @NonNull List<QCMember> othersAtFault,
                                          @NonNull List<QCMember> askersOfAnswer,
                                          @NonNull Set<QCMember> members,
                                          @NonNull List<Pair<String, String>> content) {
        // TODO : write this
        return members;
    }
}
