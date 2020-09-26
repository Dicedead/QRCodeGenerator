README

We've implemented the automatic mask selection bonus, at the end of the MatrixConstruction.java file.

To test it, simply use:
int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(VERSION, encodedData);
instead of:
int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(VERSION, encodedData,MASK);
in some main method creating the matrix.

Note that the given website, nayuki.io, frequently misses finder-like sequences worth 40 penalty points, explaining
some major differences in penalties between this program and the website's. Squares, streaks of consecutive modules of
the same colour and black module penalty should all work the same.