package pl.inh.tidy.config;

public enum SortMode {
    CATEGORY("category"),
    ALPHA("alpha"),
    COUNT("count");

    private final String id;

    SortMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static SortMode fromId(String id) {
        for (SortMode mode : values()) {
            if (mode.id.equalsIgnoreCase(id)) {
                return mode;
            }
        }
        return CATEGORY;
    }
}
