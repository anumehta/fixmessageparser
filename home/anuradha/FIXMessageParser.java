package home.anuradha;

import java.nio.ByteBuffer;
import java.util.List;

import home.anuradha.FIXMessage.FIXMessageException;

/**
 * Parser to convert {@link ByteBuffer}s into {@link FIXMessage}s. Throws exceptions if input does not conform to the
 * expected format.
 *
 */
public class FIXMessageParser {
    // Variables to hold the tag and value that are currently being parsed.
    private int tag;
    private String value;

    /**
     * Parses the ByteBuffer in a single pass. Does not flip the buffer.
     */
    public FIXMessage parse(ByteBuffer msgBB) throws FIXMessageException, Group.GroupException, RepeatingGroup.RepeatingGroupException {
        FIXMessage fixMessage = new FIXMessage();
        boolean currentTagProcessed = true; // A new tag needs to be read.
        while (msgBB.remaining() > 0 || !currentTagProcessed) {
            if(currentTagProcessed) {
                readAndSetNextTagAndValue(msgBB);
                currentTagProcessed = false; // New tag read but hasn't been processed.
            }

            if(FIXMessageUtils.isRepeatingGroupIndicatorTag(tag)) {
                // Tag 269 or 123. The value should be an integer.
                try {
                    int numberOfGroups = Integer.parseInt(value);
                    // Parse all the groups in this repeating group set.
                    this.parseRepeatingGroups(msgBB, tag, numberOfGroups, fixMessage);
                    // We do not set currentTagProcessed to true here because the parseRepeatingGroups method reads an
                    // extra tag value pair that still needs to be processed.
                } catch (NumberFormatException e) {
                    // Indicator tag value was not an integer.
                    throw new FIXMessageException(e);
                }

            } else {
                // Regular tag. Put it into the FIX message.
                fixMessage.putNonRepeatingGroupTag(tag, value);
                currentTagProcessed = true;
            }
        }

        return fixMessage;
    }

    /**
     * Extracts repeating groups from the ByteBuffer after the indicator tag has already been read. Validates all the
     * {@link Group}s and then validates the {@link RepeatingGroup} itself.
     */
    private void parseRepeatingGroups(ByteBuffer msgBB, int indicatorTag, int numberOfGroups, FIXMessage fixMessage) throws FIXMessageException, Group.GroupException, RepeatingGroup.RepeatingGroupException {
        List<Integer> groupTags = FIXMessageUtils.GROUP_TAGS.get(indicatorTag);
        Integer firstTag = groupTags.get(0);

        RepeatingGroup repeatingGroup = fixMessage.createRepeatingGroup(indicatorTag, numberOfGroups);

        readAndSetNextTagAndValue(msgBB); // Read the first tag of the first group.
        boolean stillProcessingGroup;
        while (msgBB.remaining() > 0 && tag == firstTag) { // Loop over groups.
            Group group = new Group(groupTags);
            group.put(tag, value); // Put the first tag of group.
            stillProcessingGroup = true;
            while (msgBB.remaining() > 0 && groupTags.contains(tag) && stillProcessingGroup) { // Loop over tags in a group.
                readAndSetNextTagAndValue(msgBB); // Read another tag because the previous tag belonged to the group currently being processed.
                if (tag != firstTag && groupTags.contains(tag)) {
                    // Another group tag that isn't the first tag of the next group.
                    group.put(tag, value);
                } else {
                    // Tag is either the first tag of next group or a non group tag.
                    stillProcessingGroup = false;
                }
            }
            if (!group.isValid()) {
                throw new Group.GroupException(Group.GroupException.INVALID_GROUP);
            }
            repeatingGroup.addGroup(group);
        }
        if (!repeatingGroup.isValid()) {
            throw new RepeatingGroup.RepeatingGroupException(RepeatingGroup.RepeatingGroupException.INVALID_REPEATING_GROUP);
        }
    }

    private StringBuffer sb = new StringBuffer();
    private void readAndSetNextTagAndValue(ByteBuffer msgBB) throws FIXMessageException {
        sb.setLength(0);
        try {
            tag = msgBB.getInt();
            char equalTo = msgBB.getChar();
            if (equalTo != '=') {
                throw new FIXMessageException(FIXMessageException.BAD_FORMAT);
            }

            char nextChar = msgBB.getChar();
            while (msgBB.remaining() > 0 && nextChar != '|') {
                sb.append(nextChar);
                nextChar = msgBB.getChar();
            }
            if (nextChar != '|') {
                throw new FIXMessageException(FIXMessageException.BAD_FORMAT);
            }
            value = sb.toString();
        } catch (NumberFormatException e) {
            throw new FIXMessageException(e);
        }
    }

}