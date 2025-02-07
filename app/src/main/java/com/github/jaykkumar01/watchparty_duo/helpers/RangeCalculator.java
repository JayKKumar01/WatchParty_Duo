package com.github.jaykkumar01.watchparty_duo.helpers;

import android.util.Range;

public class RangeCalculator {

    public static Range<Integer> getOptimalRange(Range<Integer>[] ranges, int fps) {
        if (ranges == null || ranges.length == 0) {
            return null; // Handle empty input
        }

        // Initialize minRange and maxRange with the first range
        Range<Integer> minRange = ranges[0];
        Range<Integer> maxRange = ranges[0];

        // First pass: Find the range with the least upper limit and the range with the maximum lower limit
        for (Range<Integer> range : ranges) {
            if (range.getUpper() < minRange.getUpper()) {
                minRange = range;
            }
            if (range.getLower() > maxRange.getLower()) {
                maxRange = range;
            }
        }

        // Second pass: Refine minRange and maxRange
        for (Range<Integer> range : ranges) {
            if (range.getUpper().equals(minRange.getUpper()) && range.getLower() < minRange.getLower()) {
                minRange = range;
            }
            if (range.getLower().equals(maxRange.getLower()) && range.getUpper() > maxRange.getUpper()) {
                maxRange = range;
            }
        }

        // FPS check conditions
        if (fps < minRange.getLower()) {
            return minRange;
        } else if (fps > maxRange.getUpper()) {
            return maxRange;
        }

        // Third pass: Find the smallest range where lower limit is greater than FPS

        return getOptimalRange(ranges, fps, maxRange);
    }

    private static Range<Integer> getOptimalRange(Range<Integer>[] ranges, int fps, Range<Integer> maxRange) {
        Range<Integer> optimalRange = maxRange;
        for (Range<Integer> range : ranges) {
            if (range.getLower() < fps) {
                continue;
            }
            if (range.getLower() < optimalRange.getLower()) {
                optimalRange = range;
            }
        }

        // Fourth pass: Refine optimal range based on upper limit
        for (Range<Integer> range : ranges) {
            if (range.getLower() < fps || range.getLower() > optimalRange.getLower()) {
                continue;
            }
            if (range.getUpper() < optimalRange.getUpper()) {
                optimalRange = range;
            }
        }
        return optimalRange;
    }
}


