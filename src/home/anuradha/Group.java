package home.anuradha;

import java.util.*;

/**
 * Represents a group inside a repeating group set in a FIX message.
 * A Group can only have a fixed set of tags, provided as a list in the constructor. It is expected that the first tag
 * in this list is the first tag of the group and the second tag in this list is a required tag (must be present in
 * group).
 * This class contains a map of tags and values of the group, maintain in order of occurrence in the message.
 * A {@link Group} must be validated after creation by calling the isValid() method.
 */
public class Group {

    public static class GroupException extends Exception {
        public static String BAD_TAG = "Invalid tag.";
        public static String TAG_ALREADY_EXISTS = "Tag already exists in group.";
        public static String INVALID_GROUP = "Invalid message. Group is missing a required tag.";

        public GroupException(String msg) {
            super(msg);
        }
    }

    private List<Integer> groupTags;
    private final Map<Integer, String> tagsAndValues = new LinkedHashMap<>(4);

    public Group(List<Integer> groupTags) {
        this.groupTags = groupTags;
    }

    public Group() {}

    public Group setGroupTags(List<Integer> groupTags) {
        this.groupTags = groupTags;
        return this;
    }

    public Map<Integer, String> getAllTagsAndValues() {
        return tagsAndValues;
    }

    public String getValue(int tag) {
        return tagsAndValues.get(tag);
    }

    public Group put(int tag, String value) throws GroupException{
        if (!groupTags.contains(tag)) {
            // Attemptng to add a tag that doesn't belong to this group.
            throw new GroupException(GroupException.BAD_TAG);
        }
        if (tagsAndValues.containsKey(tag)) {
            // Attemptng to add a tag that has already been seen.
            throw new GroupException(GroupException.TAG_ALREADY_EXISTS);
        }
        tagsAndValues.put(tag, value);

        return this;
    }

    /**
     * Validates the group by checking that it contains the first tag (groupTags.get(0)) and the required tag
     * (groupTags.get(1))
     */
    public boolean isValid() {
        return tagsAndValues.containsKey(groupTags.get(0)) && tagsAndValues.containsKey(groupTags.get(1));
    }

    public void reset() {
        tagsAndValues.clear();
        groupTags = null;
    }

    private StringBuffer sb = new StringBuffer();
    public String toString() {
        sb.setLength(0);
        sb.append("Group:[");
        for (Integer k : tagsAndValues.keySet()) {
            sb.append(k).append('=').append(tagsAndValues.get(k)).append(',');
        }
        sb.append(']');
        return sb.toString();
    }
}