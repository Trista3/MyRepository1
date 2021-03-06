package com.tryhard.myvoa;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class InformationItemManager {

	// 创建条目信息数据库

	private DBopenHelper dbOpenHelper;
	private String mTableName;

	public InformationItemManager(Context context, String tableName) {

		this.dbOpenHelper = new DBopenHelper(context);
		mTableName = tableName;
		
	}

	// 保存一条条目信息
	public void save(InformationItem informationItem) {

		if (find(informationItem.getId()) != null)
			return;

		String website = informationItem.getWebsite();
		String key = website.substring(website.length() - 10, website.length() - 5);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Bitmap bitmap = informationItem.getBitmap();

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if (null != bitmap) {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		}
	
		db.execSQL("insert into " + mTableName + "(_id,title,date,website,bitmap) values(?,?,?,?,?)",
				new Object[] { Integer.valueOf(key), informationItem.getTitle(), informationItem.getDate(),
						informationItem.getWebsite(), os.toByteArray() });
	}

	// 更新一条条目信息
	public void update(InformationItem informationItem) {
		if(null == find(informationItem.getId()))
			return;
		Bitmap  bitmap = informationItem.getBitmap();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		String website = informationItem.getWebsite();
		String key = website.substring(website.length() - 10, website.length() - 5);
		ContentValues values = new ContentValues();
		values.put("bitmap", os.toByteArray());
		db.update(mTableName, values, "_id=?", new String[]{Integer.valueOf(key).toString()});
		//db.execSQL("update " + mTableName + " set bitmap=" + os.toByteArray() + " where _id=" + Integer.valueOf(key));
	}

	// 查询条目信息
	public ArrayList<InformationItem> findAll() {
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + mTableName + " order by _id DESC", null);
		ArrayList<InformationItem> items = new ArrayList<InformationItem>();
		
		while (cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndex("_id"));
			String title = cursor.getString(1);
			String date = cursor.getString(2);
			String website = cursor.getString(3);
			byte[] bitmapByte = cursor.getBlob(4);
			Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
			InformationItem informationItem = new InformationItem(id, title, date, website, bitmap);
			items.add(informationItem);
		}

		cursor.close();
		db.close();
		return items;
	}

	// 根据参数所给的关键字，查找表中的记录
	public InformationItem find(Integer key) {
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + mTableName + " WHERE _id = " + key, null);
		InformationItem informationItem = null;
		cursor.moveToFirst();
		// 如果得到的有效的行，则生成对应的RemindingRecord记录
		if (!cursor.isAfterLast()) {

			int id = cursor.getInt(cursor.getColumnIndex("_id"));
			String title = cursor.getString(1);
			String date = cursor.getString(2);
			String website = cursor.getString(3);
			byte[] bitmapByte = cursor.getBlob(4);
			Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length, null);
			informationItem = new InformationItem(id, title, date, website, bitmap);
		}
		cursor.close();
		db.close();
		return informationItem;
	}

}
