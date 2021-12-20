package home.anuradha.concurrent;

import home.anuradha.FIXMessage;
import home.anuradha.Group;
import home.anuradha.RepeatingGroup;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FIXMessageParserWithObjectPoolingTest {

    FIXMessagePool fixMessagePool = new FIXMessagePool(1);
    RepeatingGroupPool repeatingGroupPool = new RepeatingGroupPool(2);
    GroupPool groupPool = new GroupPool(3);

    FIXMessageParserWithObjectPooling parser = new FIXMessageParserWithObjectPooling(groupPool, repeatingGroupPool, fixMessagePool);

    @Test
    public void testValidMessages() throws RepeatingGroup.RepeatingGroupException, FIXMessage.FIXMessageException, Group.GroupException {
        String msg1 = "8=345|9=12|55=IBM|40=P|269=2|277=12|456=7|283=5|277=1|231=56|456=7|44=12|";
        String msg2 = "8=345|9=12|55=IBM|40=P|123=2|786=9|398=ABC|786=QAS|567=12|496=SDF|398=12|44=12|";
        String msg3 = "8=345|9=12|55=IBM|40=P|269=2|277=12|456=7|283=5|277=1|231=56|456=7|123=2|786=9|398=ABC|786=QAS|567=12|496=SDF|398=12|44=12|";
        String msg4 = "8=345|9=12|55=IBM|40=P|269=2|277=12|456=7|283=5|277=1|231=56|456=7|100=ABC|123=2|786=9|398=ABC|786=QAS|567=12|496=SDF|398=12|44=12|";

        String res1 = "FIXMessage:[8=345,9=12,55=IBM,40=P,44=12,RepeatingGroup:[269=2,Group:[277=12,456=7,283=5,],Group:[277=1,231=56,456=7,],],]";
        String res2 = "FIXMessage:[8=345,9=12,55=IBM,40=P,44=12,RepeatingGroup:[123=2,Group:[786=9,398=ABC,],Group:[786=QAS,567=12,496=SDF,398=12,],],]";
        String res3 = "FIXMessage:[8=345,9=12,55=IBM,40=P,44=12,RepeatingGroup:[269=2,Group:[277=12,456=7,283=5,],Group:[277=1,231=56,456=7,],],RepeatingGroup:[123=2,Group:[786=9,398=ABC,],Group:[786=QAS,567=12,496=SDF,398=12,],],]";
        String res4 = "FIXMessage:[8=345,9=12,55=IBM,40=P,100=ABC,44=12,RepeatingGroup:[269=2,Group:[277=12,456=7,283=5,],Group:[277=1,231=56,456=7,],],RepeatingGroup:[123=2,Group:[786=9,398=ABC,],Group:[786=QAS,567=12,496=SDF,398=12,],],]";

        FIXMessage fixMessage1 = getFIXMessage(msg1);
        assertEquals(res1, fixMessage1.toString());
        assertEquals(0, fixMessagePool.size());
        assertEquals(1, repeatingGroupPool.size());
        assertEquals(1, groupPool.size());
        parser.returnFIXMessage(fixMessage1);
        assertEquals(1, fixMessagePool.size());
        assertEquals(2, repeatingGroupPool.size());
        assertEquals(3, groupPool.size());

        FIXMessage fixMessage2 = getFIXMessage(msg2);
        assertEquals(res2, fixMessage2.toString());
        assertEquals(0, fixMessagePool.size());
        assertEquals(1, repeatingGroupPool.size());
        assertEquals(1, groupPool.size());
        parser.returnFIXMessage(fixMessage2);
        assertEquals(1, fixMessagePool.size());
        assertEquals(2, repeatingGroupPool.size());
        assertEquals(3, groupPool.size());

        FIXMessage fixMessage3 = getFIXMessage(msg3);
        assertEquals(0, fixMessagePool.size());
        assertEquals(0, repeatingGroupPool.size());
        assertEquals(0, groupPool.size());
        assertEquals(res3, fixMessage3.toString());
        parser.returnFIXMessage(fixMessage3);
        assertEquals(1, fixMessagePool.size());
        assertEquals(2, repeatingGroupPool.size());
        assertEquals(4, groupPool.size());

        FIXMessage fixMessage4 = getFIXMessage(msg4);
        assertEquals(0, fixMessagePool.size());
        assertEquals(0, repeatingGroupPool.size());
        assertEquals(0, groupPool.size());
        assertEquals(res4, fixMessage4.toString());
        parser.returnFIXMessage(fixMessage4);
        assertEquals(1, fixMessagePool.size());
        assertEquals(2, repeatingGroupPool.size());
        assertEquals(4, groupPool.size());
    }

    @Test
    public void testTagWithNoValue() {
        String tagWithNoValue = "8=345|9=12|55=IBM|40=P|222|44=12"; // 222 has no value.
        assertThrows(FIXMessage.FIXMessageException.class, () -> getFIXMessage(tagWithNoValue));

    }

    @Test
    public void testMissingTag() {
        String missingTag = "8=345|9=12|55=IBM|40=P||44=12|"; // Empty tag between 40 and 44.
        assertThrows(FIXMessage.FIXMessageException.class, () -> getFIXMessage(missingTag));
    }

    @Test
    public void testNonIntegerTag() {
        String nonIntegerTag  = "8=345|9=12|55=IBM|40=P|TAG=VALUE|44=12|"; // A tag called "TAG" instead of a number.
        assertThrows(FIXMessage.FIXMessageException.class, () -> getFIXMessage(nonIntegerTag));
    }

    @Test
    public void testTagRepeated() {
        String nonIntegerTag  = "8=345|9=12|55=IBM|40=P|9=13|44=12|"; // 9 shows up twice.
        assertThrows(FIXMessage.FIXMessageException.class, () -> getFIXMessage(nonIntegerTag));
    }

    @Test
    public void testTooManyRepeatingGroups() {
        String tooManyRepeatingGroups  = "8=345|9=12|55=IBM|40=P|269=2|277=12|456=7|283=5|277=1|231=56|456=7|277=17|456=9|44=12|"; // 269=2 but there are 3 groups.
        RepeatingGroup.RepeatingGroupException e = assertThrows(RepeatingGroup.RepeatingGroupException.class, () -> getFIXMessage(tooManyRepeatingGroups));
        assertEquals(RepeatingGroup.RepeatingGroupException.TOO_MANY_GROUPS, e.getMessage());
    }

    @Test
    public void testTooFewRepeatingGroups() {
        String tooManyRepeatingGroups  = "8=345|9=12|55=IBM|40=P|123=3|786=9|398=ABC|786=QAS|567=12|496=SDF|398=12|44=12|"; // 123=3 but there are only 2 groups.
        RepeatingGroup.RepeatingGroupException e = assertThrows(RepeatingGroup.RepeatingGroupException.class, () -> getFIXMessage(tooManyRepeatingGroups));
        assertEquals(RepeatingGroup.RepeatingGroupException.INVALID_REPEATING_GROUP, e.getMessage());
    }

    @Test
    public void testSameIndicatorTagTwice() {
        String tooManyRepeatingGroups  = "8=345|9=12|55=IBM|40=P|269=2|277=12|456=7|283=5|277=1|231=56|456=7|100=ABC|269=2|277=12|456=7|283=5|277=1|231=56|456=7|44=12|"; // 269 shows up twice.
        FIXMessage.FIXMessageException e = assertThrows(FIXMessage.FIXMessageException.class, () -> getFIXMessage(tooManyRepeatingGroups));
        assertEquals(FIXMessage.FIXMessageException.REPEATING_GROUP_ALREADY_EXISTS, e.getMessage());
    }

    @Test
    public void testGroupMissingFirstTag() {
        String groupMissingFirstTag  = "8=345|9=12|55=IBM|40=P|123=2|398=ABC|786=QAS|567=12|496=SDF|398=12|44=12|"; // First group after 123 doesn't start with 786
        RepeatingGroup.RepeatingGroupException e = assertThrows(RepeatingGroup.RepeatingGroupException.class, () -> getFIXMessage(groupMissingFirstTag));
        assertEquals(RepeatingGroup.RepeatingGroupException.INVALID_REPEATING_GROUP, e.getMessage());
    }

    @Test
    public void testFirstTagMisplacedInGroup() {
        String firstTagMisplaced  = "8=345|9=12|55=IBM|40=P|123=2|398=ABC|786=9|567=25|786=QAS|567=12|496=SDF|398=12|44=12|"; // 786 is not the first tag after 123.
        RepeatingGroup.RepeatingGroupException e = assertThrows(RepeatingGroup.RepeatingGroupException.class, () -> getFIXMessage(firstTagMisplaced));
        assertEquals(RepeatingGroup.RepeatingGroupException.INVALID_REPEATING_GROUP, e.getMessage());
    }

    @Test
    public void testGroupMissingRequiredTag() {
        String groupMissingRequiredTag  = "8=345|9=12|55=IBM|40=P|123=2|786=9|567=25|786=QAS|567=12|496=SDF|398=12|44=12|"; // No 398 tag in first group.
        Group.GroupException e = assertThrows(Group.GroupException.class, () -> getFIXMessage(groupMissingRequiredTag));
        assertEquals(Group.GroupException.INVALID_GROUP, e.getMessage());
    }

    @Test
    public void testNonRepeatingTagInGroup() {
        String tooManyRepeatingGroups  = "8=345|9=12|55=IBM|40=P|123=2|786=9|222=BAD_TAG|398=ABC|786=QAS|567=12|496=SDF|398=12|44=12|"; // 222 shouldn't be in group.
        Group.GroupException e = assertThrows(Group.GroupException.class, () -> getFIXMessage(tooManyRepeatingGroups));
        assertEquals(Group.GroupException.INVALID_GROUP, e.getMessage());
    }

    private FIXMessage getFIXMessage(String msg) throws RepeatingGroup.RepeatingGroupException, FIXMessage.FIXMessageException, Group.GroupException {
        ByteBuffer bb1 = constructInput(msg);
        return parser.parse(bb1);
    }


    private static ByteBuffer constructInput(String msg) {
        ByteBuffer bb = ByteBuffer.allocate(1024);

        String[] tagsValues = msg.split("\\|");
        Arrays.stream(tagsValues).forEach(tv -> addTV(tv, bb));

        bb.flip();
        return bb;
    }

    private static void addTV(String tv, ByteBuffer bb) {
        if (tv!=null) {
            String[] tvsplit = tv.split("=", 2);
            try {
                int tag = Integer.parseInt(tvsplit[0]);
                bb.putInt(tag);
            } catch (NumberFormatException e) {
                char[] tagChars = tvsplit[0].toCharArray();
                for (char c : tagChars) {
                    bb.putChar(c);
                }
            }
            if (tvsplit.length == 2) {
                bb.putChar('=');
                char[] valueChars = tvsplit[1].toCharArray();
                for (char c : valueChars) {
                    bb.putChar(c);
                }
            }
        }
        bb.putChar('|');
    }

}