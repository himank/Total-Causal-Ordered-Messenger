package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class MyContentProvider extends ContentProvider {

	public static final String AUTHORITY = "content://edu.buffalo.cse.cse486586.groupmessenger.provider";
	private Context context;
	public final static Uri contentURI = Uri.parse(AUTHORITY);
	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		// contentURI = buildUri("content",
		// "edu.buffalo.cse.cse486586.groupmessenger.provider");
		context = getContext();
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub

		try {

			File myDir = new File(context.getFilesDir().getAbsolutePath());
			FileWriter fw = new FileWriter(myDir
					+ ((String) values.get(KEY_FIELD)));
			fw.write((String) values.get(VALUE_FIELD));
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return uri;

		// return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		try {
			String column[] = new String[2];
			String s;
			column[0] = selection;
			File myDir = new File(context.getFilesDir().getAbsolutePath());
			BufferedReader br = new BufferedReader(new FileReader(myDir
					+ selection));
			s = br.readLine();
			column[1] = s;
			MatrixCursor ms = new MatrixCursor(new String[] { KEY_FIELD,
					VALUE_FIELD });
			ms.addRow(column);
			br.close();
			return ms;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}