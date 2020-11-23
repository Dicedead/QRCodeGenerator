//@formatter:off
package qrcode;

import java.util.Arrays;
    //Arrays.equals method used in bonus

public class MatrixConstruction {

	/*
	 * Constants defining the color in ARGB format
	 *
	 * W = White integer for ARGB
	 *
	 * B = Black integer for ARGB
	 *
	 * both need to have their alpha component to 255
	 */

	private static final int B = 0xFF_00_00_00;
	private static final int W = 0xFF_FF_FF_FF;

	/**
	 * Create the matrix of a QR code with the given data.
	 *
	 * @param version
	 *            The version of the QR code
	 * @param data
	 *            The data to be written on the QR code
	 * @param mask
	 *            The mask used on the data. If not valid (e.g: -1), then no mask is
	 *            used.
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data, int mask) {

		/*
		 * PART 2
		 */
		int[][] matrix = constructMatrix(version, mask);
		/*
		 * PART 3
		 */
		addDataInformation(matrix, data, mask);

		return matrix;
	}

	/*
	 * =======================================================================
	 *
	 * ****************************** PART 2 *********************************
	 *
	 * =======================================================================
	 */

	/**
	 * Create a matrix (2D array) ready to accept data for a given version and mask
	 *
	 * @param version
	 *            the version number of QR code (has to be between 1 and 4 included)
	 * @param mask
	 *            the mask id to use to mask the data modules. Has to be between 0
	 *            and 7 included to have a valid matrix. If the mask id is not
	 *            valid, the modules would not be not masked later on, hence the
	 *            QRcode would not be valid
	 * @return the qrcode with the patterns and format information modules
	 *         initialized. The modules where the data should be remain empty.
	 */
	public static int[][] constructMatrix(int version, int mask) {
		int[][] finalMatrix = initializeMatrix(version);
		addFinderPatterns(finalMatrix);
		addAlignmentPatterns(finalMatrix,version);
		addTimingPatterns(finalMatrix);
		addDarkModule(finalMatrix);
		addFormatInformation(finalMatrix,mask);
		return finalMatrix;
	}

	/**
	 * Create an empty 2d array of integers of the size needed for a QR code of the
	 * given version
	 *
	 * @param version
	 *            the version number of the qr code (has to be between 1 and 4
	 *            included
	 * @return an empty matrix
	 */
	public static int[][] initializeMatrix(int version) {

		int size = QRCodeInfos.getMatrixSize(version);
		int[][] blankMatrix = new int [size][size];
		return blankMatrix;
	}

	/**
	 * Add all finder patterns to the given matrix with a border of White modules.
	 *
	 * @param matrix
	 *            the 2D array to modify: where to add the patterns
	 */
	public static void addFinderPatterns(int[][] matrix) {

		int[][] separator = createPatterns('s');
		int[][] finder    = createPatterns('f');
		final int DELTA_POS    = matrix.length-8;
		final int DELTA_POS_P1 = DELTA_POS + 1;

		final int[][] THREE_CORNERS = {{0,0,0,0},{0,DELTA_POS,0,DELTA_POS_P1},{DELTA_POS,0,DELTA_POS_P1,0}};

		for (int i = 0; i < 3; ++i)
		  {
			placePattern(matrix,separator,THREE_CORNERS[i][0],THREE_CORNERS[i][1]);
			placePattern(matrix,finder,THREE_CORNERS[i][2],THREE_CORNERS[i][3]);
		  }

	}

	/**
	 * Add the alignment pattern if needed, does nothing for version 1
	 *
	 * @param matrix
	 *            The 2D array to modify
	 * @param version
	 *            the version number of the QR code needs to be between 1 and 4
	 *            included
	 */
	public static void addAlignmentPatterns(int[][] matrix, int version) {

		if (version == 1)
		  {
			return; //No alignment pattern needed for version 1
		  }
		int[][] align = createPatterns('a');
		int coord = matrix.length-9; //top left corner of the square
		placePattern(matrix,align,coord,coord);
	}

	/**
	 * Add the timings patterns
	 *
	 * @param matrix
	 *            The 2D array to modify
	 */
	public static void addTimingPatterns(int[][] matrix) {

		for (int i = 8; i < matrix.length-7; ++i)
		{
			matrix[6][i] = matrix[i][6] = (i%2 == 0) ? B : W;
		}
	}

	/**
	 * Add the dark module to the matrix
	 *
	 * @param matrix
	 *            the 2-dimensional array representing the QR code
	 */
	public static void addDarkModule(int[][] matrix) {
		matrix[8][matrix.length-8]=B;
	}

	/**
	 * Add the format information to the matrix
	 *
	 * @param matrix
	 *            the 2-dimensional array representing the QR code to modify
	 * @param mask
	 *            the mask id
	 */
	public static void addFormatInformation(int[][] matrix, int mask) {

		final int LEN = matrix.length;
		final int LEN_9 = LEN - 9;
		final int LEN_1 = LEN - 1;
		final int LEN_10 = LEN - 10;
		final int LEN_7 = LEN - 7;

		boolean[] sequence = QRCodeInfos.getFormatSequence(mask);
		boolean addingData;

		int seqIndex1 = 0, seqIndex2 = 0;
		//seq1 and 2 are (post-)incremented every time a value in []sequence is used,
		//therefore all values in []sequence will be used exactly twice (once for
		//every format line).
		for (int i = 0; i < LEN; ++i)
		  {
			addingData = useFormatSequence(matrix, sequence[seqIndex1],i,8,8,LEN_9,
					6,seqIndex1,i);
			if (addingData)
			  {
				++seqIndex1;
			  }

			addingData = useFormatSequence(matrix, sequence[seqIndex2],8,LEN_1-i,7,
					LEN_10,LEN_7,seqIndex2,i);
			if (addingData)
			  {
				++seqIndex2;
			  }
		  }
	}

	/**
	 * Auxiliary method encoding format information as used in
	 * addFormatInformation. Its boolean return is used for
	 * seqIndex's increment (explained below).
	 *
	 * @param matrix
	 * @param seqElementI
	 *           boolean value inside the format information sequence
	 *              true ->  black
	 *              false -> white
	 * @param x,y
	 *           coordinates inside the matrix
	 * @param fillUntil
	 *           start of interval
	 * @param fillFrom
	 *           end of interval
	 * @param noFill
	 *           defines an interval in which sequence[i] is encoded,
	 *           except at index noFill
	 * @param seqIndex
	 *           seqIndex is incremented in the main method
	 *           addFormatInformation each time an element from the boolean sequence
	 *           of format info is copied into the matrix
	 * @param k
	 *           i's value in addFormatInformation
	 */
	private static boolean useFormatSequence(int[][]matrix, boolean seqElementI, int x, int y,
					int fillUntil, int fillFrom, int noFill, int seqIndex, int k) {

		if (((k<fillUntil) || (k>fillFrom)) && (k != noFill) && (seqIndex <15))
		{
			matrix[x][y] = (seqElementI) ? B : W;
			return true;
		}
		return false;
	}

	/**
	 * Place a pattern inside the main matrix at x,y coordinates
	 *
	 * @param matrix
	 *           the 2-dimensional array representing the QR code to modify
	 * @param pattern
	 *           2-dimensional array that will be placed (copied) inside the QR code
	 * @param x,y
	 *           the coordinates in the matrix of the top-left-most point
	 *           of the pattern that will be placed inside
	 */
	private static void placePattern(int[][] matrix, int[][] pattern, int y, int x) {

		for (int i = 0; i < pattern.length; ++i)
		{
			for (int j = 0; j < pattern[i].length; ++j)
			{ if (pattern[i][j] != 0)
			  {
				matrix[x+i][y+j] = pattern[i][j];
			  }
			}
		}
	}

	/**
	 * Create common/mandatory patterns shared by all QRCodes
	 *
	 * @param patternType
	 *            the desired pattern's type,
	 *               'f': finder
	 *               's': separator
	 *               'a': alignment
	 *               Other: null
	 * @return [pattern]Template
	 *            a square 2-dimensional array with the desired colour configuration and size
	 */
	private static int[][] createPatterns(char patternType) {

		if (patternType == 'f')
		  {
			//Overlapping squares takes 83 steps while only making rings
			//takes 49
			int[][] finderTemplate = new int [7][7];
			makeRing(finderTemplate,B,0);
			makeRing(finderTemplate,W,1);
			makeRing(finderTemplate,B,2);
			finderTemplate[3][3] = B;

			return finderTemplate;
		  }

		if (patternType == 's')
		  {
			//Separator = white ring later filled with the finder pattern
			int[][] separator = new int [8][8];
			makeRing(separator,W,0);

			return separator;
		  }

		if (patternType == 'a')
		  {
			int[][] alignTemplate = new int [5][5];
			makeRing(alignTemplate,B,0);
			makeRing(alignTemplate,W,1);
			alignTemplate[2][2] = B;

			return alignTemplate;
		  }

		return null;
	}

	/**
	 * Create a ring of some colour (int encoded) inside a 2-dimensional array
	 * at some position - taking less steps than filling an entire square
	 *
	 * @param squareArray
	 *            typically a pattern in construction
	 * @param colour
	 *            typically B or W
	 * @param position
	 *            position of the ring inside the square array,
	 *              0 is the outermost (square-shaped) ring
	 *              (array.length)/2 is the innermost ring (center element if odd number of
	 *              elements per line, center 2x2 square if even)
	 *              [in fact, 'position' is the 'starting position' in the for loops below]
	 */
	private static void makeRing(int[][] squareArray, int colour, int position) {
		int finalPosition = squareArray.length - position - 1;
		for (int i = position; i < squareArray.length - position; ++i)
		  {
			squareArray[position][i] = squareArray[finalPosition][i] = squareArray[i][position]
					= squareArray[i][finalPosition] = colour;
		  }
	}

	/*
	 * =======================================================================
	 * ****************************** PART 3 *********************************
	 * =======================================================================
	 */

	/**
	 * Choose the color to use with the given coordinate using the masking 0
	 *
	 * @param col
	 *            x-coordinate
	 * @param row
	 *            y-coordinate
	 * @param dataBit
	 *            initial color without masking
	 * @param masking
	 *            type of masking used
	 * @return the color with the masking
	 */
	public static int maskColor(int col, int row, boolean dataBit, int masking) {
		switch (masking)
		    {
				case 0: return assignColor(dataBit, (((col + row) % 2) == 0));
				case 1: return assignColor(dataBit, ((row % 2) == 0));
				case 2: return assignColor(dataBit, ((col % 3) == 0));
				case 3: return assignColor(dataBit, (((col + row) % 3) == 0));
				case 4: return assignColor(dataBit, (((row/2 + col/3) % 2) == 0));
				case 5: return assignColor(dataBit, (((col * row) % 2) + ((col * row) % 3) == 0));
				case 6: return assignColor(dataBit, (((((col * row) % 2) + ((col * row) % 3)) % 2) == 0));
				case 7: return assignColor(dataBit, (((((col + row) % 2) + ((col * row) % 3)) % 2) == 0));
	           default: int color = (dataBit) ? B : W; return color;
		    }
	}

	/**
	 * Auxiliary method inverting colors when the masking condition
	 * is true and leaving it as is when it is false
	 *
	 * @param dataBit
	 *           initial 'color' (in boolean form) without masking
	 * @param maskCondition
	 * @return color in ARGB format
	 */
	private static int assignColor(boolean dataBit, boolean maskCondition) {
		int color;
		if (maskCondition)
		  {
		  	color = (dataBit) ? W : B;
		  }
        else
		  {
            color = (dataBit) ? B : W;
		  }
        return color;
	}

	/**
	 * Add the data bits into the QR code matrix
	 *
	 * @param matrix
	 *            a 2-dimensional array where the bits needs to be added
	 * @param data
	 *            the data to add
	 * @param mask
	 *            applied masking
	 */
	public static void addDataInformation(int[][] matrix, boolean[] data, int mask) {
		int col, row, colLeft;
		final int LEN_1, TIMING_COLUMN;
		col = row = LEN_1 = matrix.length-1;
		TIMING_COLUMN = 6;

		int seqIndex = 0;
		final int DATA_LEN = data.length;

		boolean goUpwards = true;

		do
		  {
		  	if ((row < 0) || (row > LEN_1))
			  {
			  	row = (goUpwards) ? ++row : --row;
			  	goUpwards = !goUpwards;

			  	col -= 2;
                if (col == TIMING_COLUMN)
				  {
				    col -= 1;
				  }
			    continue;
			  }


		  	colLeft = col - 1;
		  	if (matrix[colLeft][row] == 0)
		  	// The left side is always blank whenever the right side is blank, therefore
		  	// if the left side is not blank, there is no way the right side is blank.
		  		// These statements are valid for (at least) versions 1 through 4 of QRCodes.
			  {
			    if (matrix[col][row] == 0)
			      {
			      	addData(matrix, col, row, data, mask, seqIndex, DATA_LEN);
				    ++seqIndex;
			      }
			    addData(matrix, colLeft, row, data, mask, seqIndex, DATA_LEN);
			    ++seqIndex;
			  }

		  	row = (goUpwards) ? --row : ++row;

		  } while (col >= 0);
	}

	/**
	 * Tests whether there's any data left to add to the matrix
	 * from the boolean array of data,
	 *    masks then adds it if is the case,
	 *    masks a 'false' then adds it if it isn't
	 *
	 * @param matrix
	 *          the 2 dimensional array representing the qrcode
     * @param col, row
	 *          coordinates of the element being tested
     * @param data
	 *          boolean array of data
     * @param mask
	 *          applied mask
     * @param index
	 *          index in boolean data array that will be tested
	 * @param dataLen
	 *          the boolean array of data's length
	 */
	private static void addData(int[][] matrix, int col, int row, boolean[] data,
								int mask, int index, int dataLen) {

		if (index < dataLen)
		  {
		  	matrix[col][row] = maskColor(col, row, data[index], mask);
		  }
		else
		  {
		    matrix[col][row] = maskColor(col, row, false, mask);
		  }
	}

	/*
	 * =======================================================================
	 *
	 * ****************************** BONUS **********************************
	 *
	 * =======================================================================
	 */

	/**
	 * Create the matrix of a QR code with the given data.
	 *
	 * The mask is computed automatically so that it provides the least penalty
	 *
	 * @param version
	 *            The version of the QR code
	 * @param data
	 *            The data to be written on the QR code
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data) {

		int mask = findBestMasking(version, data);

		return renderQRCodeMatrix(version, data, mask);
	}

	/**
	 * Find the best mask to apply to a QRcode so that the penalty score is
	 * minimized. Compute the penalty score with evaluate
	 *
	 * @param data
	 * @return the mask number that minimize the penalty
	 */
	public static int findBestMasking(int version, boolean[] data) {

		int temp = Integer.MAX_VALUE, mask = 0, eval;

		for (int i = 0; i < 8; ++i)
		  {
		  	int[][] tempMatrix = initializeMatrix(version);
		  	addFinderPatterns(tempMatrix);
		    addAlignmentPatterns(tempMatrix,version);
		    addTimingPatterns(tempMatrix);
		    addDarkModule(tempMatrix);
		    addFormatInformation(tempMatrix,i);
		    addDataInformation(tempMatrix, data, i);

		    eval = evaluate(tempMatrix);
		    if (temp > eval)
			{
			  temp = eval;
			  mask = i;
			}
		  }

		return mask;
	}

	/**
	 * Compute the penalty score of a matrix
	 *
	 * @param matrix:
	 *            the QR code in matrix form
	 * @return the penalty score obtained by the QR code, lower the better
	 */
	public static int evaluate(int[][] matrix) {

		final int LEN = matrix.length;
		int penalty = 0;
		int blackModules = 0;

		for (int col = 0; col < LEN; ++col)
		  {
		    for (int row = 0; row < LEN; ++row)
			  {
			  	if (matrix[col][row] == B)
				  {
				    ++blackModules;
				  }
			  	penalty += testSpecialPatterns(matrix,col,row,LEN);

			  	if (row == 0)
			 	  {
				    penalty += testConsecutive(matrix,col,true,LEN);
				  }
			  	if (col == 0)
				  {
				    penalty += testConsecutive(matrix,row,false,LEN);
				  }
			  }
		  }

		double percentageBlackModules = ((double) blackModules/(LEN*LEN))*100;
		int percentUnder, percentAbove, floorPercentageBlackMods;

		floorPercentageBlackMods = (int) percentageBlackModules;
		percentUnder = floorPercentageBlackMods - (floorPercentageBlackMods % 5);
		percentAbove = percentUnder + 5;

		percentUnder = Math.abs(percentUnder-50);
		percentAbove = Math.abs(percentAbove-50);

		penalty += Math.min(percentAbove,percentUnder)*2;

		return penalty;
	}

	/**
	 * Finds streaks of 5 or more modules of the same colour,
	 * line by line (checks every column and every row separately)
	 *
     * @param matrix
	 *            the 2-dimensional array that's being worked on
     * @param coord
	 *            starting coordinate
     * @param verticalMode
	 *            keeps track of the direction
     * @param LEN
	 *            matrix's length
     * @return
	 *            a subtotal of penalty for consecutive streaks
	 *            on the line that has just been worked on
	 */
	private static int testConsecutive(int[][] matrix, int coord, boolean verticalMode, int LEN) {

		int tempPenalty = 0;
		final int LEN_MIN4 = LEN-4;

		//Copying (readdressing or shallowcopy) the matrix's line of interest for more convenience
		int[] testedLine = new int[LEN];
		if (verticalMode)
		  {
			testedLine = matrix[coord];
		  }
		else
		  {
			for (int i = 0; i < LEN; ++i)
			  {
			    testedLine[i] = matrix[i][coord];
			  }
		  }

		int j = 0;
		for (int i = 0; i < LEN_MIN4; ++i)
		  {
		    if (testAllEqual(testedLine[i],testedLine[i+1],testedLine[i+2],
					testedLine[i+3],testedLine[i+4]))
			  {
			  	tempPenalty += 3;
			  	j = i+5; //j takes charge of testing the rest of the line
			  	while ((j < LEN) && (testedLine[j] == testedLine[j-1]))
				  {
					++tempPenalty;
					++j;
				  }
			  	i = j-1;
			  }
		  }

		return tempPenalty;
	}

	/**
	 * Testing squares and finder-like lines
	 *
	 * @param matrix
	 *         2 dimensional array of data
	 * @param col
	 *         column coordinate
	 * @param row
	 *         row coordinate
	 * @param maxLen
	 *         avoids out of bounds exceptions
	 * @return
	 *         penalty caused by the iteration of these two tests over a single
	 *         module
	 */
	private static int testSpecialPatterns(int[][] matrix, int col, int row, int maxLen) {

		int tempPenalty = 0;
		maxLen -= 1;

		//2x2 squares
		if ((col < maxLen) && (row < maxLen) //avoiding out of bounds exceptions
				&& testAllEqual(matrix[col][row],matrix[col+1][row],matrix[col][row+1],matrix[col+1][row+1]))
			    {
			    	tempPenalty += 3;
			    }

		tempPenalty += testFinderLinesPenalty(matrix,col,row, true);
		tempPenalty += testFinderLinesPenalty(matrix,col,row, false);

		return tempPenalty;
	}

	/**
	 * Looks for finder lines either vertically or horizontally
	 *
     * @param matrix
     * @param col
     * @param row
	 * @param horizontalMode
	 *          determines the orientation
     * @return
	 *          (how many lines were found, 1 or 2)*40 penalty
	 */
	private static int testFinderLinesPenalty(int[][] matrix, int col, int row, boolean horizontalMode) {

		final int LEN = matrix.length;
		final int BIG_PENALTY = 40;
		int addPenalty = 0;
		final int[] SEQUENCE_1 = {W,W,W,W,B,W,B,B,B,W,B};
		final int[] SEQUENCE_2 = {B,W,B,B,B,W,B,W,W,W,W};

		final int SEQ_LEN = SEQUENCE_1.length;
		final int STILL_INBOUNDS = 10;

		int[] testedLine = new int[SEQ_LEN];

		for (int i = 0; i < SEQ_LEN; ++i)
		  {
		  	if ((LEN - col > STILL_INBOUNDS) && (horizontalMode))
		  	  {
		  	  	testedLine[i] = matrix[col+i][row];
		  	  }
		  	else if ((LEN - row > STILL_INBOUNDS) && (!horizontalMode))
		  	  {
		  	  	testedLine[i] = matrix[col][row+i];
		  	  }
		  }

		if (Arrays.equals(SEQUENCE_1, testedLine))
		  {
		   addPenalty += BIG_PENALTY;
		  }
		if (Arrays.equals(SEQUENCE_2, testedLine))
		  {
		    addPenalty += BIG_PENALTY;
		  }

		return addPenalty;
	}

	/**
	 * Takes an arbitrarily long sequence of ints and returns whether they're all equal
	 * or not.
	 *
     * @param modules
	 *         In this case, every int is a module
     * @return
	 *         boolean value of the equality
	 */
	private static boolean testAllEqual(int... modules) {

		final int LEN_MIN1 = modules.length-1;
		int i = 0;
		do {
			if (modules[i] != modules[i+1])
			  {
			    return false;
			  }
			++i;
		} while (i < LEN_MIN1);

		return true;
	}

}
