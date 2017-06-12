package com.example.myapplication;

import com.example.myapplication.bluetooth.GameState;
import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCMember;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ModelsTest {
    @Test
    public void basicQCMemberTests() throws Exception {
        assertEquals(4, 2 + 2);

        QCMember member1 = QCMember.build("shark", false);
        QCMember member2 = QCMember.build("monkey", false);
        QCMember member3 = QCMember.build("cat", false);
        QCGameMessage msg1 = QCGameMessage.build(QCGameMessage.Action.GAME_UPDATE, GameState.PLAYING)
                .addMember(member1);
        assertEquals(1, msg1.members().size());
        assertEquals(0, msg1.members().indexOf(member1));

        // note that member1 isn't added a second time
        msg1 = msg1.addMember(member1)
                .addMember(member2)
                .addMember(member3);
        assertEquals(3, msg1.members().size());

        // they're the same because we don't look at reaction for equivalence, but the reaction HAS been updated
        QCGameMessage msg2 = msg1.setReactionPlayer(member2, QCMember.Reaction.FAILED_TO_ANSWER);
        assertEquals(msg1, msg2);
        assertEquals(QCMember.Reaction.FAILED_TO_ANSWER, msg2.members().get(1).reaction());

        // make sure order is maintained
        assertEquals(member1, msg2.members().get(0));
        assertEquals(member2, msg2.members().get(1));
        assertEquals(member3, msg2.members().get(2));

//        ObjectMapper mapper = new ObjectMapper();
//        mapper.writeValueAsBytes(msg2);
    }
}