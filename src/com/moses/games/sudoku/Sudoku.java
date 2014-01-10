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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class Sudoku extends Activity implements OnClickListener {
	private static final String TAG = "Sudoku";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SubjectDBOperator.setContext(this);
		setContentView(R.layout.main);

		// Set up click listeners for all the buttons
		View continueButton = findViewById(R.id.continue_button);
		continueButton.setOnClickListener(this);
		View newButton = findViewById(R.id.new_button);
		newButton.setOnClickListener(this);
		View aboutButton = findViewById(R.id.about_button);
		aboutButton.setOnClickListener(this);
		View exitButton = findViewById(R.id.exit_button);
		exitButton.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Music.play(this, R.raw.main);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Music.stop(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.continue_button:
			startGame(null);
			break;

		case R.id.about_button:
			Intent i = new Intent(this, About.class);
			startActivity(i);
			break;

		// More buttons go here (if any) ...
		case R.id.new_button:
			openNewGameDialog();
			break;

		case R.id.exit_button:
			finish();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			return true;
			
		case R.id.inputs:
			onInputs();
			return true;
			// More items go here (if any) ...
		}
		return false;
	}

	private void onInputs() {
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setLines(9);

		new AlertDialog.Builder(this)
			.setTitle(R.string.input_subject)
			.setView(input)
			.setPositiveButton(
				android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Log.d(TAG, "subject=" + input.getText().toString());
						doInputSubject(input.getText().toString());
					}
				})
			.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				})
			.show();
	}

	private void doInputSubject(String strSubject) {
		/** turn strSubject to strSub which meet the AutoCalc's condition */
		// strSubject = "000006001 900000376 710040000 170800003 030000010 600003058 000030065 351000002 800100000";
		if (strSubject.length() < 9 * 9) {
			return;
		}
		int c[] = new int[9 * 9];
		int i, j;
		for (i = 0, j = 0; i < strSubject.length(); i++) {
			int val = strSubject.charAt(i);
			if ('0' <= val && val <= '9' && j < 9 * 9) {
				c[j++] = val - '0';
			} 
		}
		if (j != 9 * 9) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (int ele : c) {
			sb.append(ele);
		}
		String strSub = sb.toString();

		/** do auto calculation, may spend a lot of time, show progress dialog */
		progress = ProgressDialog.show(
				this, 
				getResources().getText(R.string.input_subject), 
				getResources().getText(R.string.check_sub), 
				true
		);
		AsyncTask<String, Integer, ACResult> asyncTask = new AsyncTask<String, Integer, ACResult>() {
			@Override
			protected ACResult doInBackground(String... params) {
				ACResult res = new ACResult();
				
				/** 1. check subject existed or not */
				if (SubjectDBOperator.getInstance().subQuestionExist(params[0])) {
					res.bExisted = true;
					return res;
				}
				
				/** 2. calculate subject */
				publishProgress(R.string.calc_sub);
				res.strPuzzle = params[0];
				if (new AutoCalc().doCalc(res)) {
				/*if (true) {
					res.iDifficultyLev = 2;
					res.step = new int[81];
					for (int i = 0; i < 81; i++) {
						res.step[i] = i;
					}
					res.strResult = res.strPuzzle;*/
					/** 3. store subject to database */
					publishProgress(R.string.store_sub_to_db);
					long id = SubjectDBOperator.getInstance().insertSub(res);
					return id > 0 ? res : null;
				}
				
				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				progress.setMessage(getResources().getText(values[0]));
			}

			@Override
			protected void onPostExecute(ACResult result) {
				// this method is running on UI thread,
                // so it is safe to update UI:
				super.onPostExecute(result);
				
				if (progress != null) {
					progress.dismiss();
					progress = null;
				}
				
				if (result != null) {
					String strSuccess;
					
					if (result.bExisted) {
						strSuccess = getResources().getString(R.string.sub_exist);
					} else {
						String strFmt = Sudoku.this.getString(R.string.store_success);
						strSuccess = String.format(strFmt, result.iDifficultyLev);
					}
					Toast.makeText(Sudoku.this, strSuccess, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(Sudoku.this, R.string.store_fail, Toast.LENGTH_SHORT).show();
				}
			}
		};
		asyncTask.execute(strSub);
	}

	private ProgressDialog progress = null;

	/** Ask the user what difficulty level they want */
	private void openNewGameDialog() {
		new AlertDialog.Builder(this)
			.setTitle(R.string.new_game_title)
			.setItems(R.array.difficulty,
				new DialogInterface.OnClickListener() {
					public void onClick(
						DialogInterface dialoginterface, int i) {
							startDifficulty(i);
						}
					})
			.show();
	}
	
	/** Start a new game with the given difficulty level */
	private void startDifficulty(int i) {
		final CharSequence items[] = SubjectDBOperator.getInstance().getSubs(i);
		new AlertDialog.Builder(this)
		.setTitle(R.string.new_subject_title)
		.setItems(items,
			new DialogInterface.OnClickListener() {
				public void onClick(
					DialogInterface dialoginterface, int i) {
						startGame(items[i]);
					}
				})
		.show();
	}

	/** Start a new game with the given difficulty level */
	private void startGame(CharSequence item) {
		if (item != null) {
			Log.d(TAG, "clicked on " + item);
		}
		Intent intent = new Intent(Sudoku.this, Game.class);
		intent.putExtra(Game.KEY_DIFFICULTY, item);
		startActivity(intent);
	}
}