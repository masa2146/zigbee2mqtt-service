package com.hubbox.demo.dto;

import java.util.function.BiFunction;

public enum ComparisonOperator {
    EQUALS((a, b) -> ComparisonOperator.compareValues(a, b, true)),
    NOT_EQUALS((a, b) -> !ComparisonOperator.compareValues(a, b, true)),
    GREATER_THAN((a, b) -> ComparisonOperator.compareValues(a, b, false)),
    LESS_THAN((a, b) -> !ComparisonOperator.compareValues(a, b, false) && !ComparisonOperator.compareValues(a, b, true)),
    GREATER_THAN_OR_EQUALS((a, b) -> ComparisonOperator.compareValues(a, b, false) || ComparisonOperator.compareValues(a, b, true)),
    LESS_THAN_OR_EQUALS((a, b) -> !ComparisonOperator.compareValues(a, b, false)),
    CONTAINS((a, b) -> String.valueOf(a).contains(String.valueOf(b))),
    STARTS_WITH((a, b) -> String.valueOf(a).startsWith(String.valueOf(b))),
    ENDS_WITH((a, b) -> String.valueOf(a).endsWith(String.valueOf(b)));

    private final BiFunction<Object, Object, Boolean> operation;

    ComparisonOperator(BiFunction<Object, Object, Boolean> operation) {
        this.operation = operation;
    }

    public boolean compare(Object value1, Object value2) {
        if (value1 == null || value2 == null) {
            return false;
        }
        return operation.apply(value1, value2);
    }

    private static boolean compareValues(Object value1, Object value2, boolean equalsComparison) {
        if (value1 instanceof Boolean val1 && value2 instanceof Boolean val2) {
            return equalsComparison ? val1.equals(val2) : val1;
        }

        String s1 = String.valueOf(value1);
        String s2 = String.valueOf(value2);

        if (equalsComparison) {
            return s1.equals(s2);
        }

        try {
            double d1 = Double.parseDouble(s1);
            double d2 = Double.parseDouble(s2);
            return d1 > d2;
        } catch (NumberFormatException e) {
            return s1.compareTo(s2) > 0;
        }
    }
}
