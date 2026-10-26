package util;

/** Tiny JSON reader/writer helpers (no external library needed). */
public final class Json {

    private Json() {}

    /** Reads a top-level string/number/boolean value; "" if the key is missing. */
    public static String get(String json, String key) {
        int k = json.indexOf("\"" + key + "\"");
        if (k == -1) return "";
        int colon = json.indexOf(':', k);
        if (colon == -1) return "";
        int i = colon + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        if (i < json.length() && json.charAt(i) == '"') {
            StringBuilder v = new StringBuilder();
            boolean esc = false;
            for (int j = i + 1; j < json.length(); j++) {
                char c = json.charAt(j);
                if (esc) { v.append(c); esc = false; }
                else if (c == '\\') esc = true;
                else if (c == '"') break;
                else v.append(c);
            }
            return v.toString();
        }
        int end = i;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(i, end).trim();
    }

    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    public static Obj obj() { return new Obj(); }

    /** Fluent JSON object writer. */
    public static class Obj {
        private final StringBuilder sb = new StringBuilder("{");
        private boolean first = true;

        private Obj key(String k) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(k).append("\":");
            return this;
        }

        public Obj put(String k, String v) {
            key(k);
            if (v == null) sb.append("null"); else sb.append('"').append(esc(v)).append('"');
            return this;
        }
        public Obj put(String k, double v) {
            key(k);
            if (v == Math.rint(v)) sb.append((long) v); else sb.append(v);
            return this;
        }
        public Obj put(String k, int v) { key(k).sb.append(v); return this; }
        public Obj put(String k, boolean v) { key(k).sb.append(v); return this; }
        /** Inserts already-built JSON (object or array). */
        public Obj raw(String k, String rawJson) { key(k).sb.append(rawJson); return this; }

        @Override public String toString() { return sb.toString() + "}"; }
    }
}
