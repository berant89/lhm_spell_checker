package util;

public class DamerauLevenshteinDistance {
	
	/**
	 * Returns the edit distance needed to convert the first string into the
	 * second string
	 * 
	 * @param s1
	 *            The first string to be transformed
	 * @param s2
	 *            The second string as the transformed string
	 * @return If the return is 0, then the strings are the same, else it
	 *         returns the number of changes made from s1 to s2
	 */
	public static int computeDistance(String s1, String s2) {
		int s1_len = s1.length();
		int s2_len = s2.length();
		
		/**
		 * Create and populate the Damerau-Levenshtein matrix where,
		 * dl_matrix[i][j] holds the distance between the first i
		 * characters of s1 and the first j characters of s2.
		 */
		int[][] dl_matrix = new int[s1_len + 1][s2_len + 1];
		for (int i = 0; i <= s1_len; i++) {
			dl_matrix[i][0] = i;
		}
		for (int j = 0; j <= s2_len; j++) {
			dl_matrix[0][j] = j;
		}

		for (int i = 1; i <= s1_len; i++) {
			for (int j = 1; j <= s2_len; j++) {
				int cost = (s2.charAt(j - 1) == s1.charAt(i - 1) ? 0 : 1); //Get substitution cost
				dl_matrix[i][j] = 
						min(dl_matrix[i - 1][j] + 1, //deletion
							dl_matrix[i][j - 1] + 1, //insertion
							dl_matrix[i - 1][j - 1] + cost); //substitution
				if (i > 1 && j > 1 && (s1.charAt(i - 1) == s2.charAt(j - 2)) && (s1.charAt(i - 2) == s2.charAt(j - 1))) {
					dl_matrix[i][j] = min(dl_matrix[i][j], dl_matrix[i - 2][j - 2] + cost); //transposition
				}
			}
		}

		return dl_matrix[s1_len][s2_len];
	}
	
	/**
	 * Returns the minimum value from a array of numbers
	 * @param nums An Array of ints
	 * @return An int of the smallest value
	 */
	private static int min(int... nums) {
		int min = Integer.MAX_VALUE;
		for (int num : nums) {
			min = Math.min(min, num);
		}
		return min;
	}
}
