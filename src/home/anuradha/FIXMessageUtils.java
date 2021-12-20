package home.anuradha;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for FIX message parsing.
 */
public class FIXMessageUtils {

    public static final List<Integer> RG_INDICATOR_TAGS = Arrays.asList(123, 269);
    public static final List<Integer> RG_123_TAGS = Arrays.asList(786, 398, 567, 496);
    public static final List<Integer> RG_269_TAGS = Arrays.asList(277, 456, 231, 283);
    public static final Map<Integer, List<Integer>> GROUP_TAGS = new HashMap<>();
    static {
        GROUP_TAGS.put(123, RG_123_TAGS);
        GROUP_TAGS.put(269, RG_269_TAGS);
    }
    public static boolean isRepeatingGroupIndicatorTag(Integer tag) {
        return RG_INDICATOR_TAGS.contains(tag);
    }
}
