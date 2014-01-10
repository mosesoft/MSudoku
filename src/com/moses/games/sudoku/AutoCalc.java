
package com.moses.games.sudoku;

import java.util.Stack;

class ATile {
	protected static final int DEFAULT_VALUE = 0;		//	default value, user can't modify it
	protected static final int MODIFIABLE_VALUE = 1;	//	the value user can modify but has no calc value
	protected static final int AUTOCALC_VALUE = 2;		//  the value user can modify and has auto-calc value
	protected static final int SUPPOSE_VALUE = 3;		//  the value is supposed

	int unused[];
	int flag; // DEFAULT_VALUE, MODIFIABLE_VALUE, AUTOCALC_VALUE, SUPPOSE_VALUE
	int value;
	int step;
	
	int getValue() {
		return flag == MODIFIABLE_VALUE ? 0 : value;
	}
	
	protected static final int AC_NONE = 0;		//	do nothing
	protected static final int AC_DONE = 1;		//	auto calculate
	protected static final int AC_ERROR = 2;	//	error
	int autoCalc(int iFlag) {
		if (flag != MODIFIABLE_VALUE) {
			return AC_NONE;
		} else if (unused == null) {
			return AC_ERROR;
		} else if (unused.length == 1) {
			value = unused[0];
			flag = iFlag;
			return AC_DONE;
		}
		return AC_NONE;
	}
	
	boolean canFill(int iVal) {
		if (flag != MODIFIABLE_VALUE) {
			return false;
		}
		if (unused == null) {
			return false;
		}
		int iLen = unused.length;
		for (int i = 0; i < iLen; i++) {
			if (unused[i] == iVal) {
				return true;
			}
		}
		return false;
	}
}

class FakeTile {
	int x, y;
	int valAva[];
	int tryIdx;
	int step;
}

class ACResult {
	String  strName;	/** subject name */
	String  strPuzzle;  /** question */
	String	strResult;  /** answer */
	int step[];
	int iDifficultyLev;
	boolean bExisted;	/** existed flag */
}

class AutoCalc {
	public static final int LU = 9;	//	large unit
	public static final int SU = 3;	//	small unit
	
	private final ATile tile[][] = new ATile[LU][LU];
	private Stack<FakeTile> ftStack = new Stack<FakeTile>();
	private boolean bSuccess = false;
	int step;
	
	private boolean init(String strPuzzles) {
		if (strPuzzles.length() != LU*LU) {
			return false;
		}
		
		for (int i = 0; i < LU; ++i) {
			tile[i] = new ATile[LU];
			for (int j = 0; j < LU; ++j) {
				tile[i][j] = new ATile();
				int iValue = strPuzzles.charAt(j*LU+i) - '0';
				tile[i][j].value = iValue;
				tile[i][j].step = 0;
				if (iValue > 0) {
					tile[i][j].flag = ATile.DEFAULT_VALUE;
				} else {
					tile[i][j].flag = ATile.MODIFIABLE_VALUE;
				}
			}
		}

		autoCalcUnusedTiles();
		ftStack.clear();
		bSuccess = false;
		step = 1;
		
		return true;
	}
	
	public boolean doCalc(ACResult acRes) {
		String strPuzzles = acRes.strPuzzle;
		init(strPuzzles);
		
		if (acRes != null) {
			acRes.iDifficultyLev = Game.DIFFICULTY_EASY;
		}
		
		/** do auto calculation */
		int acFlg = ATile.AC_DONE;
		int iTileFlag = ATile.AUTOCALC_VALUE;
		while (acFlg == ATile.AC_DONE) {
			int iUnusedLen;
			iUnusedLen = SU;
			while (iUnusedLen < LU) {
				acFlg = autoCalcTiles(iUnusedLen, iTileFlag);
				if (acFlg == ATile.AC_DONE) {
					continue;
				} else if (acFlg == ATile.AC_ERROR) {
					emptySupposeTiles(false);
					autoCalcUnusedTiles();
					break;
				} else {
					if (!hasModifyableTiles()) {
						break;
					}

					iUnusedLen++;
					
					if (acRes != null && acRes.iDifficultyLev == Game.DIFFICULTY_EASY) {
						acRes.iDifficultyLev = Game.DIFFICULTY_MEDIUM;
					}
				}
			}
			
			if (acFlg == ATile.AC_NONE && isSuccess()) {
				emptySupposeTiles(true);
				break;
			}
			
			if (acFlg != ATile.AC_DONE) {
				if (acRes != null && acRes.iDifficultyLev != Game.DIFFICULTY_HARD) {
					acRes.iDifficultyLev = Game.DIFFICULTY_HARD;
				}
				// suppose to forward a new tile or back an old tile with other value 
				if (!fakeTileStep()) {
					break;
				} else {
					acFlg = ATile.AC_DONE;
					iTileFlag = ATile.SUPPOSE_VALUE;
					autoCalcUnusedTiles();
				}
			}
		}
		
		if (isSuccess()) {
			emptySupposeTiles(true);
			getResult(acRes);
			return true;
		} else {
			emptySupposeTiles(false);
			return false;
		}
	}

	private boolean isSuccess() {
		if (bSuccess) {
			return true;
		}
		if (hasModifyableTiles()) {
			return false;
		}
		
		int c[] = new int[LU];
		int i, j, val;
		// horizontal
		for (i = 0; i < LU; i++) {
			for (j = 0; j < LU; j++) {
				c[j] = 0;
			}
			for (j = 0; j < LU; j++) {
				val = tile[j][i].value;
				c[val-1] = val;
			}
			for (j = 0; j < LU; j++) {
				if (c[j] == 0) {
					return false;
				}
			}
		}

		// vertical
		for (i = 0; i < LU; i++) {
			for (j = 0; j < LU; j++) {
				c[j] = 0;
			}
			for (j = 0; j < LU; j++) {
				val = tile[i][j].value;
				c[val-1] = val;
			}
			for (j = 0; j < LU; j++) {
				if (c[j] == 0) {
					return false;
				}
			}
		}

		// same cell block
		for (int x = 0; x < SU; x++) {
			for (int y = 0; y < SU; y++) {
				for (j = 0; j < LU; j++) {
					c[j] = 0;
				}
				for (i = SU*x; i < SU*x+SU; i++) {
					for (j = SU*y; j < SU*y+SU; j++) {
						val = tile[i][j].value;
						c[val-1] = val;
					}
				}
				for (j = 0; j < LU; j++) {
					if (c[j] == 0) {
						return false;
					}
				}
			}
		}

		bSuccess = true;
		return bSuccess;
	}
	
	/** turn result into to acRes and return */
	private void getResult(ACResult acRes) {
		if (acRes == null) {
			return;
		}
		
		StringBuilder buf = new StringBuilder();
		int step[] = new int[LU*LU];
		
		for (int i = 0; i < LU; i++) {
			for (int j = 0; j < LU; j++) {
				buf.append(tile[j][i].value);
				step[j*LU+i] = tile[i][j].step;
			}
		}
		
		acRes.strResult = buf.toString();
		acRes.step = step;
	}

	/** suppose one tile step, */
	/** return true - suppose successful; false - none to suppose */
	private boolean fakeTileStep() {
		ATile it = null;
		FakeTile ft;
		boolean bError = false;
		
		// 1. try to suppose a new tile
		// 1.1 look for the min unused length
		int iMinUnusedLen = LU;
		for (int i = 0; i < LU; i++) {
			for (int j = 0; j < LU; j++) {
				it = tile[i][j];
				if (it.flag == ATile.MODIFIABLE_VALUE) {
					if (it.unused == null) {
						bError = true;
						break;
					}
					if (it.unused.length < iMinUnusedLen) {
						iMinUnusedLen = it.unused.length;
					}
				}
			}
			if (bError) {
				break;
			}
		}
		// 1.2 try the new tile with the min unused length
		if (!bError) {
			for (int i = 0; i < LU; i++) {
				for (int j = 0; j < LU; j++) {
					it = tile[i][j];
					if (it.flag == ATile.MODIFIABLE_VALUE && it.unused.length == iMinUnusedLen) {
						ft = new FakeTile();
						ft.x = i;
						ft.y = j;
						ft.valAva = new int[it.unused.length];
						for (int k = 0; k < it.unused.length; k++) {
							ft.valAva[k] = it.unused[k];
						}
						ft.tryIdx = 0;
						ft.step = step;
						ftStack.push(ft);
						
						it.step = step++;
						it.value = it.unused[0];
						it.flag = ATile.AUTOCALC_VALUE;
						return true;
					}
				}
			}
		}
		
		// 2. otherwise, suppose back an old tile
		while (!ftStack.isEmpty()) {
			ft = ftStack.peek();
			it = tile[ft.x][ft.y];
			ft.tryIdx++;
			if (ft.tryIdx < ft.valAva.length) {
				it.value = ft.valAva[ft.tryIdx];
				it.flag = ATile.AUTOCALC_VALUE;
				step = ft.step;
				emptySupposeTiles(false);
				it.step = step++;
				return true;
			}
			ftStack.pop();
			it.flag = ATile.MODIFIABLE_VALUE;
			it.value = 0;
			it.step = 0;
		}

		// else none to suppose
		return false;
	}

	// clear supposed tiles and value if necessary
	private void emptySupposeTiles(boolean success) {
		int iFlag = success ? ATile.AUTOCALC_VALUE : ATile.MODIFIABLE_VALUE;
		for (int i = 0; i < LU; i++) {
			for (int j = 0; j < LU; j++) {
				ATile it = tile[i][j];
				if (it.flag == ATile.SUPPOSE_VALUE) {
					if (!success) {
						if (it.step > step) {
							it.value = 0;
							it.step = 0;
							it.flag = iFlag;
						}
					} else {
						it.flag = iFlag;
					}
				}
			}
		}
	}
	
	private boolean hasModifyableTiles() {
		for (int i = 0; i < LU; i++) {
			for (int j = 0; j < LU; j++) {
				if (tile[i][j].flag == ATile.MODIFIABLE_VALUE) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** Compute the two dimensional array of used tiles */
	private void autoCalcUnusedTiles() {
		for (int x = 0; x < LU; x++) {
			for (int y = 0; y < LU; y++) {
				if (tile[x][y].flag == ATile.MODIFIABLE_VALUE) {
					tile[x][y].unused = autoCalcUnusedTiles(x, y);
				}
			}
		}
	}

	/** Compute the used tiles visible from this position */
	private int[] autoCalcUnusedTiles(int x, int y) {
		int c[] = new int[LU];
		// horizontal
		for (int i = 0; i < LU; i++) {
			if (i == y)
				continue;
			int t = tile[x][i].getValue();
			if (t != 0)
				c[t - 1] = t;
		}
		// vertical
		for (int i = 0; i < LU; i++) {
			if (i == x)
				continue;
			int t = tile[i][y].getValue();
			if (t != 0)
				c[t - 1] = t;
		}
		// same cell block
		int startx = (x / SU) * SU;
		int starty = (y / SU) * SU;
		for (int i = startx; i < startx + SU; i++) {
			for (int j = starty; j < starty + SU; j++) {
				if (i == x && j == y)
					continue;
				int t = tile[i][j].getValue();
				if (t != 0)
					c[t - 1] = t;
			}
		}
		// compress
		int nused = 0;
		for (int t : c) {
			if (t != 0)
				nused++;
		}
		int nunused = LU-nused;
		if (nunused > 0) {
			int c1[] = new int[nunused];
			int i, j;
			for (i = 1, j = 0; i <= LU; i++) {
				if (c[i-1] == 0) {
					c1[j++] = i;
				}
			}
			return c1;
		} else {
			return null;
		}
	}

	/** auto calculate from tiles which meet "iUnsedLen" condition */
	private int autoCalcTiles(int iUnsedLen, int iTileFlag) {
		int retAc = ATile.AC_NONE;
		int i, j;

		/** first, autoCalc each tile */
		for (i = 0; i < LU; i++) {
			for (j = 0; j < LU; j++) {
				int ac = tile[i][j].autoCalc(iTileFlag);
				if (ac == ATile.AC_ERROR) {
					retAc = ac;
					break;
				} else if (ac == ATile.AC_DONE) {
					tile[i][j].step = step++;
					retAc = ac;
				}
			}
			if (retAc == ATile.AC_ERROR) {
				break;
			}
		}
		if (retAc == ATile.AC_ERROR) {
			return retAc;
		}
		if (retAc == ATile.AC_DONE) {
			autoCalcUnusedTiles();
			return retAc;
		}

		for (i = 0; i < LU; i++) {
			for (j = 0; j < LU; j++) {
				ATile it = tile[i][j];
				if (it.flag == ATile.MODIFIABLE_VALUE && it.unused.length <= iUnsedLen) {
					// horizontal
					boolean bNext;
					int k, l; 
					for (k = 0; k < it.unused.length; k++) {
						bNext = false;
						for (l = 0; l < LU; l++) {
							if (l == j) {
								continue;
							}
							if (tile[i][l].canFill(it.unused[k])) {
								bNext = true;
								break;
							}
						}
						if (!bNext) {
							it.value = it.unused[k];
							it.flag = iTileFlag;
							it.step = step++;
							
							retAc = ATile.AC_DONE;
							autoCalcUnusedTiles();
							return retAc;
						}
					}
					
					// vertical
					for (k = 0; k < it.unused.length; k++) {
						bNext = false;
						for (l = 0; l < LU; l++) {
							if (l == i) {
								continue;
							}
							if (tile[l][j].canFill(it.unused[k])) {
								bNext = true;
								break;
							}
						}
						if (!bNext) {
							it.value = it.unused[k];
							it.flag = iTileFlag;
							it.step = step++;
							
							retAc = ATile.AC_DONE;
							autoCalcUnusedTiles();
							return retAc;
						}
					}
					
					// same cell block
					int startx = (i / SU) * SU;
					int starty = (j / SU) * SU;
					for (k = 0; k < it.unused.length; k++) {
						bNext = false;
						for (int ii = startx; ii < startx + SU; ii++) {
							for (int jj = starty; jj < starty + SU; jj++) {
								if (ii == i && jj == j)
									continue;
								if (tile[ii][jj].canFill(it.unused[k])) {
									bNext = true;
									break;
								}
							}
							if (bNext) {
								break;
							}
						}
						if (!bNext) {
							it.value = it.unused[k];
							it.flag = iTileFlag;
							it.step = step++;
							
							retAc = ATile.AC_DONE;
							autoCalcUnusedTiles();
							return retAc;
						}
					}
				}
			}
		}
		return retAc;
	}
}