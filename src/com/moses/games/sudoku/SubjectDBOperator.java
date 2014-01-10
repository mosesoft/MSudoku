
package com.moses.games.sudoku;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class SubjectDBOperator {
	static private Context ctx;
	static void setContext(Context context) {
		ctx = context;
	}
	static Context getContext() {
		return ctx;
	}
	static SubjectDBOperator subDBOp = null;
	public static SubjectDBOperator getInstance() {
		if (subDBOp == null) {
			subDBOp = new SubjectDBOperator(getContext());
		}
		return subDBOp;
	}
	
	private SubjectOpenHelper helper = null;
	private SQLiteDatabase db = null;
	
	private SubjectDBOperator(Context context) {
		helper = new SubjectOpenHelper(context);
		db = helper.getWritableDatabase();
	}
	
	public synchronized void close() {
		db.close();
		helper.close();
	}
	
	void close(Cursor cursor) {
		if (cursor != null) {
			cursor.close();
		}
	}
	
	public SQLiteDatabase getCurrentDb() {
		return db;
	}
	
	public CharSequence[] getSubs(int iDiff) {
		Cursor cursor = null;
		try {
			cursor = db.query(SubjectColumns.TABLE_NAME, 
					          new String[] {SubjectColumns.SUB_NAME},  
					          SubjectColumns.DIFFICULTY + "=?", 
					          new String[] {String.valueOf(iDiff)}, 
					          null, null, null);
			
			int count = cursor.getCount();
			CharSequence ret[] = new CharSequence[count];
			int i = 0;
			
			if (cursor.moveToFirst()) {
				do {
					String strSubName = cursor.getString(0);
					ret[i++] = strSubName.subSequence(0, strSubName.length());
				} while (cursor.moveToNext());
			}
			
			return ret;
		} finally {
			close(cursor);
		}
	}
	
	public boolean subQuestionExist(String strQuestion) {
		Cursor cursor = null;
		try {
			cursor = db.query(SubjectColumns.TABLE_NAME, 
					          new String[] {SubjectColumns.SUB_NAME},  
					          SubjectColumns.QUESTION + "=?", 
					          new String[] {strQuestion}, 
					          null, null, null);
			if (cursor.moveToFirst()) {
				return true;
			}
		} finally {
			close(cursor);
		}
		return false;
	}
	
	private String getNewSubjectName(int iDiff) {
		String strSubName;
		Cursor cursor = null;
		try {
			cursor = db.query(SubjectColumns.TABLE_NAME, 
					          new String[] {SubjectColumns._ID, SubjectColumns.SUB_NAME},  
					          SubjectColumns.DIFFICULTY + "=?", 
					          new String[] {String.valueOf(iDiff)}, 
					          null, null, SubjectColumns.SUB_NAME + " desc");
			if (cursor.moveToFirst()) {
				strSubName = cursor.getString(1);
				int iPos = strSubName.indexOf('_');
				if (iPos > 0) {
					String strLeft, strRight;
					strLeft = strSubName.substring(0, iPos+1);
					strRight = strSubName.substring(iPos+1);
					int num = Integer.parseInt(strRight) + 1;
					strSubName = strLeft + String.valueOf(num);
					return strSubName;
				}
			}
		} finally {
			close(cursor);
		}
		
		switch (iDiff) {
		case 0:
			strSubName = "E_0";
			break;
		case 1:
			strSubName = "M_0";
			break;
		case 2:
			strSubName = "H_0";
			break;
		default:
			strSubName = "E_0";
			break;
		}
		return strSubName;
	}
	
	/** 
	 * add a new subject 
	 * 
	 * @return Id of the new created subject.
	 */
	public long insertSub(ACResult res) {
		/** get new subject name by difficulty */
		res.strName = getNewSubjectName(res.iDifficultyLev);

		ContentValues cv = new ContentValues();
		cv.put(SubjectColumns.SUB_NAME, res.strName);
		cv.put(SubjectColumns.DIFFICULTY, res.iDifficultyLev);
		cv.put(SubjectColumns.QUESTION, res.strPuzzle);
		cv.put(SubjectColumns.ANSWER, res.strResult);
		
		/** convert step info */
		int iStep[] = res.step;
		int stepLen = iStep.length;
		byte[] stepBuf = new byte[stepLen];
		for (int i = 0; i < stepLen; i++) {
			stepBuf[i] = (byte)iStep[i];
		}
		cv.put(SubjectColumns.STEP, stepBuf);
		
		return db.insert(SubjectColumns.TABLE_NAME, null, cv);
	}
	
	public ACResult selectSubject(String strSubName) {
		Cursor cursor = null;
		try {
			cursor = db.query(SubjectColumns.TABLE_NAME, 
					          SubjectColumns.ALL_COLUMNS, 
					          SubjectColumns.SUB_NAME + "=?", 
					          new String[] {strSubName}, 
					          null, null, null);
			if (cursor.moveToFirst()) {
				return mappingToACResult(cursor);
			}
			return null;
		} finally {
			close(cursor);
		}
	}
	
	private ACResult mappingToACResult(Cursor cursor) {
		ACResult res = new ACResult();
		res.strName = cursor.getString(1);
		res.iDifficultyLev = (int)cursor.getLong(2);
		res.strPuzzle = cursor.getString(3);
		res.strResult = cursor.getString(4);
		
		/** convert step info */
		byte[] stepBuf = cursor.getBlob(5);
		int stepLen = stepBuf.length;
		int iStep[] = new int[stepLen];
		for (int i = 0; i < stepLen; i++) {
			iStep[i] = (int)stepBuf[i];
		}
		res.step = iStep;
		
		return res;
	}
}