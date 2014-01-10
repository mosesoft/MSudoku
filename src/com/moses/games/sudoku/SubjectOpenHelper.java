
package com.moses.games.sudoku;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SubjectOpenHelper extends SQLiteOpenHelper {

	/** easy level */
	public static final String QE = "360000000004230800000004200"
			+ "070460003820000014500013020" + "001900000007048300000000045";
	private static final String AE = "362581479914237856785694231179462583823759614546813927431925768657148392298376145";
	private static final byte SE[] = 
		{0, 0, 12, 42, 45, 46, 25, 50, 51, 41, 16, 0, 0, 0, 40, 0, 48, 52, 37, 17, 15, 43, 35, 0, 0, 49, 53, 2, 0, 1, 0, 0, 
		 7, 5, 8, 0, 0, 0, 6, 44, 39, 47, 19, 0, 0, 0, 3, 4, 18, 0, 0, 21, 0, 27, 9, 10, 0, 0, 28, 24, 20, 23, 32, 30, 11, 0, 
		 34, 0, 0, 0, 26, 29, 33, 14, 13, 38, 31, 36, 22, 0, 0};
		/*{0, 0, 9, 41, 42, 43, 22, 50, 51, 44, 13, 0, 0, 0, 37, 0, 45, 52, 38, 12, 11, 46, 33, 0, 0, 47, 
		53, 2, 0, 1, 0, 0, 6, 3, 7, 0, 0, 0, 8, 48, 39, 49, 14, 0, 0, 0, 4, 5, 23, 0, 0, 16, 0, 18, 28, 
		24, 0, 0, 25, 19, 15, 17, 29, 30, 26, 0, 34, 0, 0, 0, 20, 27, 35, 31, 10, 40, 32, 36, 21, 0, 0}; */
	public static final String SubNameEasy = "E_0";
	
	/** medium level */
	public static final String QM = "650000070000506000014000005"
			+ "007009000002314700000700800" + "500000630000201000030000097";
	private static final String AM = "659123478783546912214978365347859126862314759195762843528497631976231584431685297";
	private static final byte SM[] = 
		{0, 0, 25, 3, 30, 24, 28, 0, 37, 51, 53, 26, 0, 40, 0, 35, 48, 43, 38, 0, 0, 8, 34, 23, 22, 1, 0, 
		 33, 54, 0, 7, 4, 0, 31, 41, 49, 45, 55, 0, 0, 0, 0, 0, 2, 56, 46, 39, 29, 0, 6, 5, 0, 36, 50, 0, 
		 18, 16, 9, 15, 13, 0, 0, 20, 52, 47, 27, 0, 17, 0, 32, 42, 44, 19, 0, 12, 10, 11, 14, 21, 0, 0};
		/*{0, 0, 25, 3, 30, 24, 28, 0, 37, 51, 53, 26, 0, 40, 0, 35, 48, 43, 38, 0, 0, 8, 34, 23, 22, 1, 0, 
		 33, 54, 0, 7, 4, 0, 31, 41, 49, 45, 55, 0, 0, 0, 0, 0, 2, 56, 46, 39, 29, 0, 6, 5, 0, 36, 50, 0, 
		 18, 16, 9, 15, 13, 0, 0, 20, 52, 47, 27, 0, 17, 0, 32, 42, 44, 19, 0, 12, 10, 11, 14, 21, 0, 0};*/
	public static final String SubNameMedium = "M_0";
	
	/** hard level */
	public static final String QH = "009000000080605020501078000"
			+ "000000700706040102004000000" + "000720903090301080000000600";
	private static final String AH = "649132578387695421521478369832916745756843192914257836168724953495361287273589614";
	private static final byte SH[] = 
		{20, 24, 0, 29, 34, 19, 53, 36, 56, 5, 0, 6, 0, 35, 0, 7, 0, 38, 0, 27, 0, 33, 0, 0, 9, 25, 31, 
		 48, 44, 41, 45, 50, 21, 0, 42, 54, 0, 18, 0, 1, 0, 17, 0, 22, 0, 52, 39, 0, 49, 46, 4, 51, 37, 
		 55, 23, 28, 10, 0, 0, 13, 0, 30, 0, 12, 0, 8, 0, 11, 0, 3, 0, 15, 26, 40, 32, 16, 2, 14, 0, 47, 43};
	public static final String SubNameHard = "H_0";

	
	public SubjectOpenHelper(Context context) {
		super(context, "sudoku.db", null, 2);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql_create_subs = cat(
				"CREATE TABLE ", 
				SubjectColumns.TABLE_NAME, 
				" (", 
				SubjectColumns._ID, " INTEGER PRIMARY KEY AUTOINCREMENT, ", 
				SubjectColumns.SUB_NAME, " VARCHAR(256), ", 
				SubjectColumns.DIFFICULTY, " INTEGER, ", 
				SubjectColumns.QUESTION, " VARCHAR(256), ", 
				SubjectColumns.ANSWER, " VARCHAR(256), ", 
				SubjectColumns.STEP, " BLOB ", 
				");"
		);

		String sql_index_subs_name = cat(
				"CREATE UNIQUE INDEX IDX_", 
				SubjectColumns.TABLE_NAME, 
				"_", 
				SubjectColumns.SUB_NAME, 
				" ON ", 
				SubjectColumns.TABLE_NAME, 
				" ( ", 
				SubjectColumns.SUB_NAME, 
				");"
		);
		
		db.execSQL(sql_create_subs);
		db.execSQL(sql_index_subs_name);
		
		ContentValues cv = new ContentValues();
		
		/** insert easy level */
		cv.put(SubjectColumns.SUB_NAME, SubNameEasy);
		cv.put(SubjectColumns.DIFFICULTY, 0);
		cv.put(SubjectColumns.QUESTION, QE);
		cv.put(SubjectColumns.ANSWER, AE);
		cv.put(SubjectColumns.STEP, SE);
		db.insert(SubjectColumns.TABLE_NAME, null, cv);

		/** insert medium level */
		cv.put(SubjectColumns.SUB_NAME, SubNameMedium);
		cv.put(SubjectColumns.DIFFICULTY, 1);
		cv.put(SubjectColumns.QUESTION, QM);
		cv.put(SubjectColumns.ANSWER, AM);
		cv.put(SubjectColumns.STEP, SM);
		db.insert(SubjectColumns.TABLE_NAME, null, cv);

		/** insert hard level */
		cv.put(SubjectColumns.SUB_NAME, SubNameHard);
		cv.put(SubjectColumns.DIFFICULTY, 2);
		cv.put(SubjectColumns.QUESTION, QH);
		cv.put(SubjectColumns.ANSWER, AH);
		cv.put(SubjectColumns.STEP, SH);
		db.insert(SubjectColumns.TABLE_NAME, null, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			db.execSQL(cat("DROP TABLE ", SubjectColumns.TABLE_NAME));
			onCreate(db);
		}
	}
	
	String cat(String... ss) {
		StringBuilder sb = new StringBuilder(ss.length << 3);
		for (String s : ss) {
			sb.append(s);
		}
		return sb.toString();
	}
}