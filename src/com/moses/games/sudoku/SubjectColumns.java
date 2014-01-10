
package com.moses.games.sudoku;

import android.provider.BaseColumns;

public interface SubjectColumns extends BaseColumns {
	static final String TABLE_NAME = "subject";
	
	static final String SUB_NAME = "sub_name";
	
	static final String DIFFICULTY = "diff";
	
	static final String QUESTION = "question";
	
	static final String ANSWER = "answer";
	
	static final String STEP = "step";
	
	static final String ALL_COLUMNS[] = {_ID, SUB_NAME, DIFFICULTY, QUESTION, ANSWER, STEP };
}