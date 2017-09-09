package sixarmstudios.quizletcolors.logic.engine;

import android.support.v4.util.Pair;

import com.example.myapplication.bluetooth.QCMember;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public class DistributionLogicTest {
    @Test
    public void test_allocateContent() {
        QCMember member1 = QCMember.build("shark", true);
        QCMember member2 = QCMember.build("monkey", false);
        QCMember member3 = QCMember.build("cat", false);
        QCMember member4 = QCMember.build("snake", false);

        List<QCMember> members = Arrays.asList(member1, member2, member3, member4);
        List<Pair<String, String>> content = Arrays.asList(
                new Pair<>("q1", "a1"),
                new Pair<>("q2", "a2"),
                new Pair<>("q3", "a3"),
                new Pair<>("q4", "a4"),
                new Pair<>("q5", "a5"),
                new Pair<>("q6", "a6"),
                new Pair<>("q7", "a7"),
                new Pair<>("q8", "a8"),
                new Pair<>("q9", "a9"),
                new Pair<>("q10", "a10"),
                new Pair<>("q11", "a11"),
                new Pair<>("q12", "a12"),
                new Pair<>("q13", "a13")
        );
        IDistributionLogic logic = new DistributionLogic();
        List<QCMember> allocatedSet = logic.allocateContent(5, members, content);
        assertEquals(4, allocatedSet.size());
        for (QCMember member : allocatedSet) {
            assertNotNull(member.question());
            assertEquals(5, member.options().size());
        }

        allocatedSet = logic.allocateContent(3, members, content);
        assertEquals(4, allocatedSet.size());
        for (QCMember member : allocatedSet) {
            assertNotNull(member.question());
            assertEquals(3, member.options().size());
        }
//        assertEquals("s", allocatedSet);

        Set<String> questions = new HashSet<>();
        questions.add(allocatedSet.get(0).question());
        questions.add(allocatedSet.get(1).question());
        questions.add(allocatedSet.get(2).question());
        questions.add(allocatedSet.get(3).question());
        assertEquals(4, questions.size());  // everyone should have a unique question

        Set<String> answers = new HashSet<>();
        answers.addAll(allocatedSet.get(0).options());
        answers.addAll(allocatedSet.get(1).options());
        answers.addAll(allocatedSet.get(2).options());
        answers.addAll(allocatedSet.get(3).options());

        for (String question : questions) {
            String hackyAnswerCheck = question.replace('q', 'a');
            assertTrue("Failed to find '" + hackyAnswerCheck + "' in  '" + answers + "'", answers.contains(hackyAnswerCheck));
        }
    }

    @Test
    public void test_updateForCorrectMove() {
        List<Pair<String, String>> content = Arrays.asList(
                new Pair<>("q0", "a0"),
                new Pair<>("q1", "a1"),
                new Pair<>("q2", "a2"),
                new Pair<>("q3", "a3"),
                new Pair<>("q4", "a4"),
                new Pair<>("q5", "a5"),
                new Pair<>("q6", "a6"),
                new Pair<>("q7", "a7"),
                new Pair<>("q8", "a8"),
                new Pair<>("q9", "a9"),
                new Pair<>("q10", "a10"),
                new Pair<>("q11", "a11"),
                new Pair<>("q12", "a12"),
                new Pair<>("q13", "a13")
        );

        QCMember member1 = QCMember.build("one", true, "r", "q0", Arrays.asList("a1", "a2", "a3"));
        QCMember member2 = QCMember.build("two", false, "g", "q1", Arrays.asList("a0", "a1", "a4"));
        QCMember member3 = QCMember.build("thr", false, "b", "q2", Arrays.asList("a4", "a5", "a6"));
        QCMember member4 = QCMember.build("fou", false, "c", "q3", Arrays.asList("a0", "a8", "a9"));

        List<QCMember> members = new ArrayList<>(Arrays.asList(member1, member2, member3, member4));

        IDistributionLogic logic = new DistributionLogic();
        List<QCMember> allocatedSet = logic.updateForCorrectMove(member1, member2, "a0", members, content);
        // only the asker and answerer changed
        assertFalse(member1.equals(allocatedSet.get(0)));
        assertFalse(member2.equals(allocatedSet.get(1)));
        assertTrue(member3.equals(allocatedSet.get(2)));
        assertTrue(member4.equals(allocatedSet.get(3)));


        // everyone has the same number of options
        assertEquals(member1.options().size(), allocatedSet.get(0).options().size());
        assertEquals(member2.options().size(), allocatedSet.get(1).options().size());
        assertEquals(member3.options().size(), allocatedSet.get(2).options().size());
        assertEquals(member4.options().size(), allocatedSet.get(3).options().size());

        // asker only changes their question
        assertFalse(member1.question().equals(allocatedSet.get(0).question()));
        assertTrue(member1.options().equals(allocatedSet.get(0).options()));

        // answerer only changes their answered option
        assertTrue(member2.question().equals(allocatedSet.get(1).question()));
        assertFalse(member2.options().equals(allocatedSet.get(1).options()));
        assertEquals(3, allocatedSet.get(1).options().size());
        assertTrue(allocatedSet.get(1).options().contains("a1"));
        assertTrue(allocatedSet.get(1).options().contains("a4"));


        Set<String> questions = new HashSet<>();
        questions.add(allocatedSet.get(0).question());
        questions.add(allocatedSet.get(1).question());
        questions.add(allocatedSet.get(2).question());
        questions.add(allocatedSet.get(3).question());
        assertEquals(4, questions.size());  // everyone should have a unique question

        Set<String> answers = new HashSet<>();
        answers.addAll(allocatedSet.get(0).options());
        answers.addAll(allocatedSet.get(1).options());
        answers.addAll(allocatedSet.get(2).options());
        answers.addAll(allocatedSet.get(3).options());

        for (String question : questions) {
            String hackyAnswerCheck = question.replace('q', 'a');
            assertTrue("Failed to find '" + hackyAnswerCheck + "' in  '" + answers + "'", answers.contains(hackyAnswerCheck));
        }
    }

    @Test
    public void test_populateContentLists() {
        List<Pair<String, String>> content = Arrays.asList(
                new Pair<>("q0", "a0"),
                new Pair<>("q1", "a1"),
                new Pair<>("q2", "a2"),
                new Pair<>("q3", "a3")
        );
        Map<String, Pair<String, String>> answerLookup = new HashMap<>();
        Map<String, Pair<String, String>> questionLookup = new HashMap<>();
        DistributionLogic logic = new DistributionLogic();

        logic.populateContentLists(content, answerLookup, questionLookup);
        assertEquals(4, answerLookup.size());
        assertEquals(4, questionLookup.size());

        assertEquals(content.get(0), answerLookup.get("a0"));
        assertEquals(content.get(0), questionLookup.get("q0"));
        assertNull(answerLookup.get("ash"));
    }

    @Test
    public void test_selectNewOption_everythingIsOnBoard() {
        List<Pair<String, String>> allContent = Arrays.asList(
                new Pair<>("q0", "a0"),
                new Pair<>("q1", "a1"),
                new Pair<>("q2", "a2"),
                new Pair<>("q3", "a3")
        );

        DistributionLogic logic = new DistributionLogic();
        List<Pair<String, String>> stuffNotOnTheBoard = new ArrayList<>();
        List<String> optionsForMember = new ArrayList<>();
        optionsForMember.addAll(Arrays.asList("a0", "a1"));
        logic.selectNewOption(stuffNotOnTheBoard, allContent, optionsForMember, "a1");

        assertTrue("Retains old value 'a0' :" + optionsForMember, optionsForMember.indexOf("a0") > -1);
        assertTrue("Does not have old value 'a1' :" + optionsForMember, optionsForMember.indexOf("a1") == -1);
        assertEquals("All options are unique : " + optionsForMember, 2, new HashSet<>(optionsForMember).size());
        assertEquals(0, stuffNotOnTheBoard.size());
    }
    @Test
    public void test_selectNewOption_oneThingNotOnBoard() {
        List<Pair<String, String>> allContent = Arrays.asList(
                new Pair<>("q0", "a0"),
                new Pair<>("q1", "a1"),
                new Pair<>("q2", "a2"),
                new Pair<>("q3", "a3")
        );

        DistributionLogic logic = new DistributionLogic();
        List<Pair<String, String>> stuffNotOnTheBoard = new ArrayList<>();
        stuffNotOnTheBoard.add(allContent.get(3));
        List<String> optionsForMember = new ArrayList<>();
        optionsForMember.addAll(Arrays.asList("a0", "a1"));
        logic.selectNewOption(stuffNotOnTheBoard, allContent, optionsForMember, "a1");

        assertEquals(2, optionsForMember.size());
        assertTrue("Retains old value 'a0' :" + optionsForMember, optionsForMember.indexOf("a0") > -1);
        assertTrue("Does not have old value 'a1' :" + optionsForMember, optionsForMember.indexOf("a1") == -1);
        assertTrue("Grabs the only item not on the board :" + optionsForMember, optionsForMember.indexOf("a3") > -1);
        assertEquals("All options are unique : " + optionsForMember, 2, new HashSet<>(optionsForMember).size());
        assertEquals(0, stuffNotOnTheBoard.size());
    }

    @Test
    public void test_selectNewOption_everythingOnBoard() {
        List<Pair<String, String>> allContent = Arrays.asList(
                new Pair<>("q0", "a0"),
                new Pair<>("q1", "a1"),
                new Pair<>("q2", "a2"),
                new Pair<>("q3", "a3")
        );

        DistributionLogic logic = new DistributionLogic();
        List<Pair<String, String>> stuffNotOnTheBoard = new ArrayList<>();
        List<String> optionsForMember = new ArrayList<>();
        optionsForMember.addAll(Arrays.asList("a0", "a1"));
        logic.selectNewOption(stuffNotOnTheBoard, allContent, optionsForMember, "a1");

        assertEquals(2, optionsForMember.size());
        assertTrue("Retains old value 'a0' :" + optionsForMember, optionsForMember.indexOf("a0") > -1);
        assertTrue("Does not have old value 'a1' :" + optionsForMember, optionsForMember.indexOf("a1") == -1);
        assertEquals("All options are unique : " + optionsForMember, 2, new HashSet<>(optionsForMember).size());
    }

    @Test
    public void test_updateForBadMove_2player_giveOtherPlayerBadAnswer() {
        List<Pair<String, String>> content = Arrays.asList(
                new Pair<>("q0", "a0"),
                new Pair<>("q1", "a1"),
                new Pair<>("q2", "a2"),
                new Pair<>("q3", "a3")
        );

        QCMember member1 = QCMember.build("one", true, "r", "q0", Arrays.asList("a1", "a2"));
        QCMember member2 = QCMember.build("two", false, "g", "q1", Arrays.asList("a0", "a3"));

        List<QCMember> members = new ArrayList<>(Arrays.asList(member1, member2));

        IDistributionLogic logic = new DistributionLogic();
        // m2 gives "a3" to m1. Nobody wants that.  m1 WANTED "a0" (had by m1)
        List<QCMember> allocatedSet = logic.updateForBadMove(member1, member2,
                "a3", "a0",
                Arrays.asList(member2), Collections.emptyList(),
                members, content);

        assertEquals(2, allocatedSet.size());
        assertFalse(member1.equals(allocatedSet.get(0)));
        assertFalse("Options should have changed away from: \n" + member2 + " :: " + allocatedSet.get(1), member2.equals(allocatedSet.get(1)));

        assertTrue(member1.username().equals(allocatedSet.get(0).username()));
        assertTrue(member2.username().equals(allocatedSet.get(1).username()));

        // everyone has the same number of options
        assertEquals(member1.options().size(), allocatedSet.get(0).options().size());
        assertEquals(member2.options().size(), allocatedSet.get(1).options().size());

        // asker only changes their question
        assertFalse(member1.question().equals(allocatedSet.get(0).question()));
        assertTrue(member1.options().equals(allocatedSet.get(0).options()));
        assertEquals(member1.options().size(), new HashSet<>(allocatedSet.get(0).options()).size());

        // person at fault got a new answer option
        assertTrue(member2.question().equals(allocatedSet.get(1).question()));
        assertFalse(member2.options().equals(allocatedSet.get(1).options()));
        assertEquals(member2.options().size(), new HashSet<>(allocatedSet.get(1).options()).size());

    }


    @Test
    public void test_updateForBadMove_2player_giveOtherPlayerYourAnswer() {
        List<Pair<String, String>> content = Arrays.asList(
                new Pair<>("q0", "a0"),
                new Pair<>("q1", "a1"),
                new Pair<>("q2", "a2"),
                new Pair<>("q3", "a3")
        );

        QCMember member1 = QCMember.build("one", true, "r", "q0", Arrays.asList("a0", "a2"));
        QCMember member2 = QCMember.build("two", false, "g", "q1", Arrays.asList("a3", "a1"));

        List<QCMember> members = new ArrayList<>(Arrays.asList(member1, member2));

        IDistributionLogic logic = new DistributionLogic();
        // m2 gives "a3" to themselves. They HAD the right answer "a2" and fucked it up. M1 has nothing going on.
        List<QCMember> allocatedSet = logic.updateForBadMove(member2, member2,
                "a3", "a1",
                Arrays.asList(member2), Collections.emptyList(),
                members, content);

        assertEquals(2, allocatedSet.size());
        assertEquals(member1, allocatedSet.get(0));
        assertFalse("Options should have changed away from: \n" + member2, member2.equals(allocatedSet.get(1)));

        assertTrue(member1.username().equals(allocatedSet.get(0).username()));
        assertTrue(member2.username().equals(allocatedSet.get(1).username()));

        // everyone has the same number of options
        assertEquals(member1.options().size(), allocatedSet.get(0).options().size());
        assertEquals(member2.options().size(), allocatedSet.get(1).options().size());

        // idiot has a new question and new answers
        assertFalse(member2.question().equals(allocatedSet.get(1).question()));
        assertFalse(member2.options().equals(allocatedSet.get(1).options()));
        assertEquals(member2.options().size(), new HashSet<>(allocatedSet.get(1).options()).size());
        assertEquals("incorrect answer is gone", -1, allocatedSet.get(1).options().indexOf("a3"));
        assertEquals("old correct answer has been wiped",-1, allocatedSet.get(1).options().indexOf("a1"));
    }

    @Test
    public void test_updateForBadMove() {
        List<Pair<String, String>> content = Arrays.asList(
                new Pair<>("q0", "a0"),
                new Pair<>("q1", "a1"),
                new Pair<>("q2", "a2"),
                new Pair<>("q3", "a3"),
                new Pair<>("q4", "a4"),
                new Pair<>("q5", "a5"),
                new Pair<>("q6", "a6"),
                new Pair<>("q7", "a7"),
                new Pair<>("q8", "a8"),
                new Pair<>("q9", "a9"),
                new Pair<>("q10", "a10"),
                new Pair<>("q11", "a11"),
                new Pair<>("q12", "a12"),
                new Pair<>("q13", "a13")
        );

        QCMember member1 = QCMember.build("one", false, "r", "q0", Arrays.asList("a1", "a2", "a3"));
        QCMember member2 = QCMember.build("two", false, "g", "q1", Arrays.asList("a5", "a1", "a2"));
        QCMember member3 = QCMember.build("thr", false, "b", "q2", Arrays.asList("a4", "a5", "a12"));
        QCMember member4 = QCMember.build("fou", false, "c", "q3", Arrays.asList("a0", "a8", "a9"));
        QCMember member5 = QCMember.build("fou", false, "c", "q12", Arrays.asList("a4", "a8", "a9"));

        List<QCMember> members = new ArrayList<>(Arrays.asList(member1, member2, member3, member4, member5));

        IDistributionLogic logic = new DistributionLogic();
        // m2 gives "a2" to m1 (SHOULD have given to m3).  m1 WANTED "a0" (had by m4)
        List<QCMember> allocatedSet = logic.updateForBadMove(member1, member2,
                "a2", "a0",
                Arrays.asList(member4), Arrays.asList(member3),
                members, content);

        // everyone shuffles except for member5 who wasn't involved in anything
        assertFalse(member1.equals(allocatedSet.get(0)));
        assertFalse(member2.equals(allocatedSet.get(1)));
        assertFalse(member3.equals(allocatedSet.get(2)));
        assertFalse(member4.equals(allocatedSet.get(3)));
        assertTrue(member5.equals(allocatedSet.get(4)));

        // everyone has the same number of options
        assertEquals(member1.options().size(), allocatedSet.get(0).options().size());
        assertEquals(member2.options().size(), allocatedSet.get(1).options().size());
        assertEquals(member3.options().size(), allocatedSet.get(2).options().size());
        assertEquals(member4.options().size(), allocatedSet.get(3).options().size());
        assertEquals(member5.options().size(), allocatedSet.get(4).options().size());

        // asker only changes their question
        assertFalse(member1.question().equals(allocatedSet.get(0).question()));
        assertTrue(member1.options().equals(allocatedSet.get(0).options()));

        // answerer only changes their answered option
        assertTrue(member2.question().equals(allocatedSet.get(1).question()));
        assertFalse(member2.options().equals(allocatedSet.get(1).options()));

        // askers of answer provided get a new question
        assertFalse(member3.question().equals(allocatedSet.get(2).question()));
        assertTrue(member3.options().equals(allocatedSet.get(2).options()));

        // others at fault got a new answer option
        assertTrue(member4.question().equals(allocatedSet.get(3).question()));
        assertFalse(member4.options().equals(allocatedSet.get(3).options()));


        Set<String> questions = new HashSet<>();
        questions.add(allocatedSet.get(0).question());
        questions.add(allocatedSet.get(1).question());
        questions.add(allocatedSet.get(2).question());
        questions.add(allocatedSet.get(3).question());
        assertEquals(4, questions.size());  // everyone should have a unique question

        Set<String> answers = new HashSet<>();
        answers.addAll(allocatedSet.get(0).options());
        answers.addAll(allocatedSet.get(1).options());
        answers.addAll(allocatedSet.get(2).options());
        answers.addAll(allocatedSet.get(3).options());

//        assertFalse(answers.contains("a2"));    // these answers should have been junked
//        assertFalse(answers.contains("a0"));
        assertFalse(questions.contains("q0"));  // these questions should have been junked
        assertFalse(questions.contains("q2"));

        for (String question : questions) {
            String hackyAnswerCheck = question.replace('q', 'a');
            assertTrue("Failed to find '" + hackyAnswerCheck + "' in  '" + answers + "'", answers.contains(hackyAnswerCheck));
        }
    }
}
