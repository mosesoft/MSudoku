/***
 * Excerpted from "Hello, Android!",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband for more book information.
 ***/
package com.moses.games.sudoku;

import com.moses.games.sudoku.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

class ItemTile {
	/** default value, user can't modify it, it already existed */
	boolean IsDefault() {
		return calcStep == 0;
	}
	
	int puzzle;
	int used[];
	int calcVal;
	int calcStep;
}

public class Game extends Activity {
	private final ItemTile itemTile[][] = new ItemTile[9][9];

	private SubjectDBOperator subop;
	
	/** begin auto calculate and play members */
	public static final int GMODE_USER = 0;
	public static final int GMODE_AUTOPLAY = 1;
	private int gmode = GMODE_USER;
	public int getGMode() { 
		return gmode;
	}
	private int apStep = 1;  // auto play step
	/** end auto calculate and play members */

	
	private static final String TAG = "Sudoku";
	public static final String KEY_DIFFICULTY = "com.moses.games.sudoku.difficulty";
	private static final String PREF_PUZZLE = "puzzle";
	private static final String PREF_SUBJECT = "subject";

	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_MEDIUM = 1;
	public static final int DIFFICULTY_HARD = 2;

	protected static final int DIFFICULTY_CONTINUE = -1;

	private PuzzleView puzzleView;
	private String strSubjectName;

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu1, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.AutoPlay:
			beginAutoPlay();
			return true;
		}
		return false;
	}
	
	private void emptyTilesInitState() {
		for (int i = 0; i < 9; ++i) {
			for (int j = 0; j < 9; ++j) {
				ItemTile it = itemTile[i][j];
				if (!it.IsDefault()) {
					it.puzzle = 0;
				}
			}
		}
		calculateUsedTiles();
	}
	
	private void beginAutoPlay() {
		if (gmode == GMODE_AUTOPLAY) {
			return;
		}
		
		/** empty tiles to initial state */
		emptyTilesInitState();
		
		gmode = GMODE_AUTOPLAY;
		apStep = 1;
		autoPlay();
	}
	
	Handler viewHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (gmode == GMODE_AUTOPLAY) {
				Game.this.autoPlay();
			}
		}
	};
	
	private void autoPlay() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				ItemTile it = itemTile[i][j];
				if (it.calcStep == apStep) {
					puzzleView.select(i, j);
					setTileIfValid(i, j, it.calcVal);
					apStep++;
					puzzleView.invalidate();
					
					viewHandler.removeMessages(0);
					viewHandler.sendMessageDelayed(viewHandler.obtainMessage(0), 1000);
					
					return;
				}
			}
		}
	}
	
	public void stopAutoPlay() {
		gmode = GMODE_USER;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		strSubjectName = null;
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			strSubjectName = bundle.getString(KEY_DIFFICULTY);
		}

		String strPuzzle = null;
		if (strSubjectName == null) {
			//	not new game, but continue
			SharedPreferences pref = getPreferences(MODE_PRIVATE);
			strSubjectName = pref.getString(PREF_SUBJECT, SubjectOpenHelper.SubNameEasy);
			strPuzzle = pref.getString(PREF_PUZZLE, SubjectOpenHelper.QE);
		}

		subop = SubjectDBOperator.getInstance();
		ACResult res = subop.selectSubject(strSubjectName);
		if (res != null) {
			recreateTiles(res);
			if (strPuzzle != null) {
				int puzzle[] = fromPuzzleString(strPuzzle);
				for (int i = 0; i < 9; i++) {
					for (int j = 0; j < 9; j++) {
						itemTile[i][j].puzzle = puzzle[i+j*9];
					}
				}
			}
			calculateUsedTiles();
		}

		/** begin test */
		/*String strPuzzle = "000006001900000376710040000170800003030000010600003058000030065351000002800100000";
		res.strPuzzle = SubjectOpenHelper.QH; // strPuzzle; 
		if (new AutoCalc().doCalc(res)) {
			Log.d(TAG, "lev=" + res.iDifficultyLev + ";ans=" + res.strResult);
			String strRes = res.strResult;
			int step[] = res.step;
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					int iValue = strRes.charAt(j*9+i) - '0';
					ItemTile it = itemTile[i][j];
					it.calcVal = iValue;
					it.calcStep = step[j*9+i];
				}
			}
		}*/
		/** end test */

		puzzleView = new PuzzleView(this);
		setContentView(puzzleView);
		puzzleView.requestFocus();

		// ...
		// If the activity is restarted, do a continue next time
		getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);
	}
	
	private void recreateTiles(ACResult res) {
		String strRes = res.strResult;
		int step[] = res.step;
		for (int i = 0; i < 9; i++) {
			itemTile[i] = new ItemTile[9];
			for (int j = 0; j < 9; j++) {
				itemTile[i][j] = new ItemTile();
				ItemTile it = itemTile[i][j];
				int iValue = strRes.charAt(j*9+i) - '0';
				it.calcVal = iValue;
				it.calcStep = step[j*9+i];
				if (it.calcStep == 0) {
					it.puzzle = iValue;
				}
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Music.play(this, R.raw.game);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		Music.stop(this);

		// Save the current puzzle
		int puzzle[] = new int[9*9];
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				puzzle[j*9+i] = itemTile[i][j].puzzle;
			}
		}
		getPreferences(MODE_PRIVATE)
			.edit()
			.putString(PREF_PUZZLE, toPuzzleString(puzzle))
			.putString(PREF_SUBJECT, strSubjectName)
			.commit();
	}

	/** Convert an array into a puzzle string */
	static private String toPuzzleString(int[] puz) {
		StringBuilder buf = new StringBuilder();
		for (int element : puz) {
			buf.append(element);
		}
		return buf.toString();
	}

	/** Convert a puzzle string into an array */
	static protected int[] fromPuzzleString(String string) {
		int[] puz = new int[string.length()];
		for (int i = 0; i < puz.length; i++) {
			puz[i] = string.charAt(i) - '0';
		}
		return puz;
	}

	/** Return the tile at the given coordinates */
	private int getTile(int x, int y) {
		// return puzzle[y * 9 + x];
		return itemTile[x][y].puzzle;
	}

	/** Change the tile at the given coordinates */
	private void setTile(int x, int y, int value) {
		// puzzle[y * 9 + x] = value;
		itemTile[x][y].puzzle = value;
	}

	/** Return a string for the tile at the given coordinates */
	protected String getTileString(int x, int y) {
		int v = getTile(x, y);
		if (v == 0)
			return "";
		else
			return String.valueOf(v);
	}

	/** Return a string for the tile at the given coordinates */
	protected String getTileCalcString(int x, int y) {
		int v = itemTile[x][y].calcVal;
		if (v == 0)
			return "";
		else
			return String.valueOf(v);
	}

	/** Return a flag for the tile at the given coordinates */
	protected boolean isTileDefault(int x, int y) {
		return itemTile[x][y].IsDefault();
	}
	
	/** Change the tile only if it's a valid move */
	protected boolean setTileIfValid(int x, int y, int value) {
		int tiles[] = getUsedTiles(x, y);
		if (value != 0) {
			for (int tile : tiles) {
				if (tile == value)
					return false;
			}
		}
		setTile(x, y, value);
		calculateUsedTiles();
		return true;
	}

	/** Open the keypad if there are any valid moves */
	protected void showKeypadOrError(int x, int y) {
		if (isTileDefault(x, y)) {
			return;
		}

		int tiles[] = getUsedTiles(x, y);
		if (tiles.length == 9) {
			Toast toast = Toast.makeText(this, R.string.no_moves_label,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} else {
			Log.d(TAG, "showKeypad: used=" + toPuzzleString(tiles));
			Dialog v = new Keypad(this, tiles, puzzleView);
			v.show();
		}
	}

	/** Cache of used tiles */
	// private final int used[][][] = new int[9][9][];

	/** Return cached used tiles visible from the given coords */
	protected int[] getUsedTiles(int x, int y) {
		// return used[x][y];
		return itemTile[x][y].used;
	}

	/** Compute the two dimensional array of used tiles */
	private void calculateUsedTiles() {
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				itemTile[x][y].used = calculateUsedTiles(x, y);
				// used[x][y] = calculateUsedTiles(x, y);
				// Log.d(TAG, "used[" + x + "][" + y + "] = "
				// + toPuzzleString(used[x][y]));
			}
		}
	}

	/** Compute the used tiles visible from this position */
	private int[] calculateUsedTiles(int x, int y) {
		int c[] = new int[9];
		// horizontal
		for (int i = 0; i < 9; i++) {
			if (i == y)
				continue;
			int t = getTile(x, i);
			if (t != 0)
				c[t - 1] = t;
		}
		// vertical
		for (int i = 0; i < 9; i++) {
			if (i == x)
				continue;
			int t = getTile(i, y);
			if (t != 0)
				c[t - 1] = t;
		}
		// same cell block
		int startx = (x / 3) * 3;
		int starty = (y / 3) * 3;
		for (int i = startx; i < startx + 3; i++) {
			for (int j = starty; j < starty + 3; j++) {
				if (i == x && j == y)
					continue;
				int t = getTile(i, j);
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
		int c1[] = new int[nused];
		nused = 0;
		for (int t : c) {
			if (t != 0)
				c1[nused++] = t;
		}
		return c1;
	}
}
