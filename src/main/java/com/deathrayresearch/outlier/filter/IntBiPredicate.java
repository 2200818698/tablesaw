package com.deathrayresearch.outlier.filter;

/**
 *
 */
public interface IntBiPredicate {

  /**
   * Returns true if valueToTest meets the criteria of this predicate when valueToCompareAgainst is considered
   *
   * Example (to compare all the values v in a column such that v > 4, v is the value to test and 4 is the value to
   * compare against
   *
   * @param valueToTest             the value you're checking. Often this is the value of a cell in an int column
   * @param valueToCompareAgainst   the value to compare against. Often this is a single value for all comparisions
   */
  boolean test(int valueToTest, int valueToCompareAgainst);
}
