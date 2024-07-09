package uk.gov.companieshouse.pscstatementdataapi.logging;

import uk.gov.companieshouse.logging.util.DataMap;

import java.util.Map;

public class DataMapHolder {

    private DataMapHolder() {}

    private static final ThreadLocal<DataMap.Builder> DATAMAP_BUILDER = ThreadLocal.withInitial(
            () -> new DataMap.Builder().requestId("uninitialised"));

    public static void initialise(String requestId) {
        DATAMAP_BUILDER.get().requestId(requestId);
    }

    public static void clear() {
        DATAMAP_BUILDER.remove();
    }

    public static DataMap.Builder get() {
        return DATAMAP_BUILDER.get();
    }

    /**
     * Used to populate the log context map in structured logging. e.g.
     * <code>
     *     logger.error("Something happened", DataMapHolder.getLogMap());
     * </code>
     * @return Structured logging DataMap.Builder
     */
    public static Map<String, Object> getLogMap() {
        return DATAMAP_BUILDER.get()
                .build()
                .getLogMap();
    }
}
