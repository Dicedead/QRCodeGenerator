package qrcode;

import java.nio.charset.StandardCharsets;
import reedsolomon.ErrorCorrectionEncoding;

public final class DataEncoding {

	/**
	 * Apply encoding methods to input string
	 *
	 * @param input
	 * @param version
	 * @return
	 *         encoded message in boolean[] format
	 */
	public static boolean[] byteModeEncoding(String input, int version) {

		final int MAX_CHAR_LENGTH = QRCodeInfos.getMaxInputLength(version);
		final int CORRECTION_BYTES = QRCodeInfos.getECCLength(version);
		final int FINAL_LENGTH = QRCodeInfos.getCodeWordsLength(version);

		int[] encodedMessage = encodeString(input, MAX_CHAR_LENGTH);
		encodedMessage = addInformations(encodedMessage);
		encodedMessage = fillSequence(encodedMessage,FINAL_LENGTH);
		encodedMessage = addErrorCorrection(encodedMessage,CORRECTION_BYTES);

		return bytesToBinaryArray(encodedMessage);
	}

	/**
	 * @param input
	 *            The string to convert to ISO-8859-1
	 * @param maxLength
	 *          The maximal number of bytes to encode (will depend on the version of the QR code) 
	 * @return A array that represents the input in ISO-8859-1. The output is
	 *         truncated to fit the version capacity
	 */	
	public static int[] encodeString(String input, int maxLength) {

		byte[] byteMessage = input.getBytes(StandardCharsets.ISO_8859_1);
		final int BYTE_LEN = byteMessage.length;

		int minimumLength = Math.min(BYTE_LEN, maxLength);
		// (instead of Math.min to avoid using an external library)

		int[]  encodedMessage = new int[minimumLength];

		for (int i = 0; i < minimumLength ; ++i)
		  {
		  	encodedMessage[i] = byteMessage[i];
		  }
		
		return encodedMessage;
	}

	/**
	 * Add the 12 bits information data and concatenate the bytes to it
	 * 
	 * @param inputBytes
	 *            the data byte sequence
	 * @return The input bytes with an header giving the type and size of the data
	 */
	public static int[] addInformations(int[] inputBytes) {

		final int INPUT_LEN = inputBytes.length;

		int byteMode = 0b0100;
		int lastBit = 0b0000;

		int bitsLength1 = INPUT_LEN >>4 & 0xF;
		int bitsLength2 = INPUT_LEN & 0xF;

		int[]byteSequence = new int[INPUT_LEN+2];
		
		byteSequence[0] = byteMode << 4 | bitsLength1; // byte 0 
		byteSequence[1]= bitsLength2 << 4;
				
		for (int j = 0; j < INPUT_LEN ;++j)
		  {
			int bitsLessSignificant = inputBytes[j]>>4 & 0xF;
			int bitsMoreSignificant = (inputBytes[j] & 0xF);

			byteSequence[j+1] = byteSequence[j+1] | bitsLessSignificant;
			byteSequence[j+2] = bitsMoreSignificant << 4;
		  }

		byteSequence[INPUT_LEN+1] = byteSequence[INPUT_LEN+1] | lastBit;

		return byteSequence;
	}

	/**
	 * Add padding bytes to the data until the size of the given array matches the
	 * finalLength
	 * 
	 * @param encodedData
	 *            the initial sequence of bytes
	 * @param finalLength
	 *            the minimum length of the returned array
	 * @return an array of length max(finalLength,encodedData.length) padded with
	 *         bytes 236,17
	 */
	public static int[] fillSequence(int[] encodedData, int finalLength) {

		final int ENC_DATA_LEN = encodedData.length;
		final int FILL_LENGTH = finalLength - ENC_DATA_LEN;
		int [] encodedDataFilled = new int [finalLength];

		System.arraycopy(encodedData, 0, encodedDataFilled, 0, ENC_DATA_LEN);
			for (int i=0; i < FILL_LENGTH ; ++i)
			  {
				if (i%2 == 0)
				  {
				  	encodedDataFilled[i+ENC_DATA_LEN] = 236;
				  }
				else
				  {
				  	encodedDataFilled[i+ENC_DATA_LEN] = 17;
				  }
			  }
		return encodedDataFilled;
	}
	/**
	 * Add the error correction to the encodedData
	 * 
	 * @param encodedData
	 *            The byte array representing the data encoded
	 * @param eccLength
	 *            the version of the QR code
	 * @param  
	 * @return the original data concatenated with the error correction
	 */
	public static int[] addErrorCorrection(int[] encodedData, int eccLength) {
		final int ENC_DATA_LEN = encodedData.length;
		int[] encodedDataCorrected = new int [ENC_DATA_LEN+eccLength];

		System.arraycopy(encodedData, 0, encodedDataCorrected, 0, ENC_DATA_LEN);

		int[] encodings = ErrorCorrectionEncoding.encode(encodedData, eccLength);

		if (eccLength >= 0) System.arraycopy(encodings, 0, encodedDataCorrected, ENC_DATA_LEN, eccLength);

		return encodedDataCorrected;
	}

	/**
	 * Encode the byte array into a binary array represented with boolean using the
	 * most significant bit first.
	 * 
	 * @param data
	 *            an array of bytes
	 * @return a boolean array representing the data in binary
	 */
	public static boolean[] bytesToBinaryArray(int[] data) {
		final int BYTES_LENGTH = 8;
		final int DATA_LEN = data.length;

		boolean[] booleanValues = new boolean[DATA_LEN*8];
		int seqIndex = 0;

		for (int datum : data) {
			for (int k = BYTES_LENGTH - 1; k >= 0; --k) {
				int valueChar = datum >> k & 1;
				if (valueChar == 0) {
					booleanValues[seqIndex] = false;
				} else //valueChar == 1
				{
					booleanValues[seqIndex] = true;
				}
				++seqIndex;
			}
		}
		return booleanValues;
	}

	}
