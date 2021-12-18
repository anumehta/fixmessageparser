package home.anuradha;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a repeating group in a FIX message.
 * This is a shell around all {@link Group} objects that come under a single indicator tag (269 or 123)
 * Contains the indicator tag, the number of groups expected in this repeating group (i.e. indicator tag value),
 * and a list of {@link Group}s. A {@link RepeatingGroup} must be validated after creation by calling the
 * isValid() method.
 */
public class RepeatingGroup {

    public static class RepeatingGroupException extends Exception {
        public static String TOO_MANY_GROUPS = "Invalid message. Too many groups in a repeating group set.";
        public static String INVALID_REPEATING_GROUP = "Invalid message. Too few groups in a repeating group set.";

        public RepeatingGroupException(String msg) {
            super(msg);
        }
    }

    private final int indicatorTag;
    private final int numberOfGroups;
    private final List<Group> groups;

    public RepeatingGroup(int indicatorTag, int numberOfGroups) {
        this.indicatorTag = indicatorTag;
        this.numberOfGroups = numberOfGroups;
        groups = new ArrayList<>(numberOfGroups);
    }

    public void addGroup(Group group) throws RepeatingGroupException {
        if (groups.size() >= numberOfGroups) {
            throw new RepeatingGroupException(RepeatingGroupException.TOO_MANY_GROUPS);
        }

        groups.add(group);
    }

    public int getIndicatorTag() {
        return indicatorTag;
    }

    public int getNumberOfGroups() {
        return numberOfGroups;
    }

    public List<Group> getGroups() {
        return groups;
    }

    /**
     * Validates the repeating group by ensuring that the number of groups added are equal to the number expected.
     */
    public boolean isValid() {
        return groups.size() == numberOfGroups;
    }

    private StringBuffer sb = new StringBuffer();
    public String toString() {
        sb.setLength(0);
        sb.append("RepeatingGroup:[");
        sb.append(indicatorTag).append('=').append(numberOfGroups).append(',');
        for (Group g : groups) {
            sb.append(g.toString()).append(',');
        }
        sb.append(']');
        return sb.toString();
    }
}
