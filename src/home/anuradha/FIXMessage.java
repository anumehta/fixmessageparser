package home.anuradha;

import java.util.LinkedHashMap;
import java.util.Map;

import static home.anuradha.FIXMessageUtils.*;

/**
 * Represents a FIX message.
 * It contains a map of all regular (non-group) tags that maintains the order in which the tags
 * appeared in the message.
 * Repeating groups are stored in a map of the indicator tag (269 or 123) to a {@link RepeatingGroup} object.
 */
public class FIXMessage {

    public static class FIXMessageException extends Exception {
        public static String BAD_FORMAT = "Invalid message format.";
        public static String TAG_ALREADY_EXISTS = "Tag already exists.";
        public static String NOT_INDICATOR_TAG = "Not an indicator tag.";
        public static String REPEATING_GROUP_ALREADY_EXISTS = "Repeating group already exists.";
        public static String NON_POSITIVE_GROUPS = "Cannot create less than one group.";

        public FIXMessageException(String msg) {
            super(msg);
        }

        public FIXMessageException(Exception e) {
            super(e);
        }
    }

    private final Map<Integer, String> nonRepeatingTagsAndValues = new LinkedHashMap<>();

    private final Map<Integer, RepeatingGroup> repeatingGroups = new LinkedHashMap<>(RG_INDICATOR_TAGS.size());

    /**
     * Put a tag that doesn't belong to any repeating group (i.e. group tags and indicator tags). Method caller must
     * validate the tag.
     */
    public void putNonRepeatingGroupTag(Integer tag, String value) throws FIXMessageException {
        if (nonRepeatingTagsAndValues.containsKey(tag)) {
            // Attempting to put a tag that already exists.
            throw new FIXMessageException(FIXMessageException.TAG_ALREADY_EXISTS);
        }
        nonRepeatingTagsAndValues.put(tag, value);
    }

    /**
     * Add a {@link RepeatingGroup} instance if it checks out based on the indicatorTag and numberOfGroups.
     * Called when an indicator tag is first read. Caller can then create and add all groups into the returned object.
     */
    public void addRepeatingGroup(RepeatingGroup repeatingGroup) throws FIXMessageException {
        Integer indicatorTag = repeatingGroup.getIndicatorTag();
        Integer numberOfGroups = repeatingGroup.getNumberOfGroups();
        if (!RG_INDICATOR_TAGS.contains(indicatorTag)) {
            // Attempting to create a repeating group with an invalid indicator tag.
            throw new FIXMessageException(FIXMessageException.NOT_INDICATOR_TAG);
        }
        if (numberOfGroups <= 0) {
            // Attempting to create less than one group.
            throw new FIXMessageException(FIXMessageException.NON_POSITIVE_GROUPS);
        }
        if(repeatingGroups.containsKey(indicatorTag)) {
            // Attempting to create repeating group with an indicator tag that has already been seen.
            throw new FIXMessageException(FIXMessageException.REPEATING_GROUP_ALREADY_EXISTS);
        }
        repeatingGroups.put(indicatorTag, repeatingGroup);
    }

    /**
     * Get the value of any tag that doesn't belong to a group. This includes indicator tags.
     */
    public String getNonGroupTagValue(Integer tag) {
        if (repeatingGroups.containsKey(tag)) {
            // Indicator tag. Return numberOfGroups as a String.
            return String.valueOf(repeatingGroups.get(tag).getNumberOfGroups());
        }
        return nonRepeatingTagsAndValues.get(tag);
    }

    // Getters

    public Map<Integer, String> getAllNonRepeatingTagsAndValues() {
        return nonRepeatingTagsAndValues;
    }

    public RepeatingGroup getRepeatingGroup(Integer indicatorTag) {
        return repeatingGroups.get(indicatorTag);
    }

    public Map<Integer, RepeatingGroup> getRepeatingGroups() {
        return repeatingGroups;
    }

    public void reset() {
        nonRepeatingTagsAndValues.clear();
        repeatingGroups.clear();
    }


    private StringBuffer sb = new StringBuffer();
    public String toString() {
        sb.setLength(0);
        sb.append("FIXMessage:[");
        for (Integer k : nonRepeatingTagsAndValues.keySet()) {
            sb.append(k).append('=').append(nonRepeatingTagsAndValues.get(k)).append(',');
        }
        for (RepeatingGroup rg : repeatingGroups.values()) {
            sb.append(rg.toString()).append(',');
        }
        sb.append(']');
        return sb.toString();
    }
}
