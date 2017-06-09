package sixarmstudios.quizletcolors;


import android.support.v4.util.Pair;

import com.example.myapplication.bluetooth.QCMember;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import sixarmstudios.quizletcolors.logic.engine.GameEngine;
import sixarmstudios.quizletcolors.logic.engine.IGameEngine;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class GameEngineTest {
    QCMember member1 = QCMember.build("shark", "red", "q1", Arrays.asList("a1", "a2", "a3", "a4"));
    QCMember member2 = QCMember.build("monkey", "green", "q6", Arrays.asList("a2", "a3", "a4", "a5"));
    QCMember member3 = QCMember.build("cat", "blue", "q7", Arrays.asList("a2", "a3", "a4"));
    QCMember member4 = QCMember.build("rock", "white", "q1", Arrays.asList("a3", "a4", "a5", "a6"));

    private IGameEngine getBasicEngine() {
        IGameEngine engine = new GameEngine();
        engine.addMember(member1);
        engine.addMember(member2);
        engine.addMember(member3);
        engine.addMember(member4);
        engine.setContent(Arrays.asList(
                new Pair<>("q1", "a1"),
                new Pair<>("q2", "a2"),
                new Pair<>("q3", "a3"),
                new Pair<>("q4", "a4"),
                new Pair<>("q5", "a5"),
                new Pair<>("q6", "a6"),
                new Pair<>("q7", "a7"),
                new Pair<>("q8", "a8")
        ));
        return engine;
    }

    @Test
    public void test_basics() throws Exception {
        IGameEngine engine = getBasicEngine();
        assertEquals(member3, engine.findMemberByColor("blue"));
        assertNull(engine.findMemberByColor("asfdas"));

        assertEquals(member2, engine.findMemberByUsername("monkey"));
        assertNull(engine.findMemberByUsername("Joe"));
    }

    @Test
    public void test_getPlayersWithAnswer() {
        IGameEngine engine = getBasicEngine();
        assertEquals(Arrays.asList(member1, member2, member3), engine.getPlayersWithAnswer("a2"));
        assertEquals(Arrays.asList(member1, member2, member4, member3), engine.getPlayersWithAnswer("a3"));
        assertEquals(Collections.singletonList(member4), engine.getPlayersWithAnswer("a6"));
        assertEquals(Collections.emptyList(), engine.getPlayersWithAnswer("a8"));
        assertEquals(Collections.emptyList(), engine.getPlayersWithAnswer("asldfk"));
    }

    @Test
    public void test_getAnswerForPlayer() throws Exception {
        IGameEngine engine = getBasicEngine();
        assertEquals("a1", engine.getAnswerForPlayer(member1));
        assertEquals("a6", engine.getAnswerForPlayer(member2));
        assertEquals("a7", engine.getAnswerForPlayer(member3));
        assertEquals("a1", engine.getAnswerForPlayer(member4));
    }

    @Test
    public void test_getQuestionForAnswer() throws Exception {
        IGameEngine engine = getBasicEngine();
        assertEquals("q1", engine.getQuestionForAnswer("a1"));
        assertEquals("q2", engine.getQuestionForAnswer("a2"));
        assertEquals("q3", engine.getQuestionForAnswer("a3"));
        assertEquals("q8", engine.getQuestionForAnswer("a8"));
    }


    @Test
    public void test_askersLookingForAnswer() throws Exception {
        IGameEngine engine = getBasicEngine();
        assertEquals(Arrays.asList(member1, member4), engine.askersLookingForAnswer("a1"));
        assertEquals(Collections.singletonList(member2), engine.askersLookingForAnswer("a6"));
        assertEquals(Collections.emptyList(), engine.askersLookingForAnswer("a8"));
        assertEquals(Collections.emptyList(), engine.askersLookingForAnswer("asldfk"));
    }

}