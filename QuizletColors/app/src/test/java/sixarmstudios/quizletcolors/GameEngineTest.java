package sixarmstudios.quizletcolors;


import android.support.v4.util.Pair;

import com.example.myapplication.bluetooth.GameState;
import com.example.myapplication.bluetooth.ImmutableQCPlayerMessage;
import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCMember;
import com.example.myapplication.bluetooth.QCMove;
import com.example.myapplication.bluetooth.QCPlayerMessage;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import sixarmstudios.quizletcolors.logic.engine.GameEngine;
import sixarmstudios.quizletcolors.logic.engine.IGameEngine;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class GameEngineTest {
    QCMember member1 = QCMember.build("shark", false, "red", "q1", Arrays.asList("a1", "a2", "a3", "a4"));
    QCMember member2 = QCMember.build("monkey", false, "green", "q6", Arrays.asList("a2", "a3", "a4", "a5"));
    QCMember member3 = QCMember.build("cat", true, "blue", "q7", Arrays.asList("a2", "a3", "a4","a4"));
    QCMember member4 = QCMember.build("rock", false, "white", "q1", Arrays.asList("a3", "a4", "a5", "a6"));

    private IGameEngine getBasicEngine() {
        IGameEngine engine = new GameEngine();
        engine.addMember(member1);
        engine.addMember(member2);
        engine.addMember(member3);
        engine.addMember(member4);
        engine.setContent("mock set name", Arrays.asList(
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
        assertEquals(Arrays.asList(member1, member2, member3, member4), engine.getPlayersWithAnswer("a3"));
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


    @Test
    public void test_move_bad_bad_answer() throws Exception {
        IGameEngine engine = getBasicEngine();
        QCPlayerMessage msg = ImmutableQCPlayerMessage.builder()
                .state(GameState.PLAYING)
                .action(QCPlayerMessage.Action.PLAYER_MOVE)
                .move(QCMove.build("a2", "red"))    // p1 : shark : red was looking for A1
                .username("monkey")                               // no one is looking for A2
                .build();
        QCGameMessage result = engine.processMessage(msg);
        assertNotNull(result);
        assertEquals(QCGameMessage.Action.BAD_ANSWER, result.action());
        assertEquals("q2", result.question());
        assertEquals("a2", result.providedAnswer());
        assertEquals("red", result.providedColor());
        assertEquals("green", result.answererColor());
        assertEquals(null, result.answeredQuestionColor());
        assertEquals("red", result.correctAnswerColor());
        assertEquals(null, result.questionColor());
        assertEquals("a1", result.correctAnswer());
        assertEquals("q1", result.answeredQuestion());

        assertEquals(QCMember.Reaction.RECEIVED_BAD_ANSWER, result.members().get(0).reaction());
        assertEquals(QCMember.Reaction.WRONG_CHOICE, result.members().get(1).reaction());
        assertEquals(QCMember.Reaction.NONE, result.members().get(2).reaction());
        assertEquals(QCMember.Reaction.NONE, result.members().get(3).reaction());

        assertEquals(4, result.members().get(0).options().size());
        assertEquals(4, result.members().get(1).options().size());
        assertEquals(4, result.members().get(2).options().size());
        assertEquals(4, result.members().get(3).options().size());
    }

    @Test
    public void test_move_bad() throws Exception {
        IGameEngine engine = getBasicEngine();
        QCPlayerMessage msg = ImmutableQCPlayerMessage.builder()
                .state(GameState.PLAYING)
                .action(QCPlayerMessage.Action.PLAYER_MOVE)
                .move(QCMove.build("a6", "red"))    // p1 : shark : red was looking for A1
                .username("rock")                                 // p2 : monkey : green was looking for A6
                .build();
        QCGameMessage result = engine.processMessage(msg);
        assertNotNull(result);
        assertEquals(QCGameMessage.Action.WRONG_USER, result.action());
        assertEquals("q6", result.question());
        assertEquals("a6", result.providedAnswer());
        assertEquals("red", result.providedColor());
        assertEquals("white", result.answererColor());
        assertEquals("green", result.answeredQuestionColor());
        assertEquals("red", result.correctAnswerColor());
        assertEquals(null, result.questionColor());
        assertEquals("a1", result.correctAnswer());
        assertEquals("q1", result.answeredQuestion());

        assertEquals(QCMember.Reaction.RECEIVED_BAD_ANSWER, result.members().get(0).reaction());
        assertEquals(QCMember.Reaction.FAILED_TO_RECEIVE_ANSWER, result.members().get(1).reaction());
        assertEquals(QCMember.Reaction.NONE, result.members().get(2).reaction());
        assertEquals(QCMember.Reaction.WRONG_USER, result.members().get(3).reaction());

        assertEquals(4, result.members().get(0).options().size());
        assertEquals(4, result.members().get(1).options().size());
        assertEquals(4, result.members().get(2).options().size());
        assertEquals(4, result.members().get(3).options().size());
    }

}