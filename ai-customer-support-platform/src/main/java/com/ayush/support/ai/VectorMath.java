package com.ayush.support.ai;

import java.util.Locale;

public final class VectorMath {
    private VectorMath() {}

    public static String toPgVector(java.util.List<Float> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(String.format(Locale.ROOT, "%.8f", values.get(i)));
        }
        return sb.append(']').toString();
    }
}
