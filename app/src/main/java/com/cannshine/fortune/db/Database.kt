package com.cannshine.fortune.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.cannshine.fortune.model.Hexagram
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class Database(private val mContext: Context) : SQLiteOpenHelper(mContext, DB_NAME, null, 1) {
    var db: SQLiteDatabase? = null
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {}
    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}
    fun createDB() {
        val dbExist = checkDB()
        if (dbExist) {
        } else {
            this.readableDatabase
            close()
            try {
                copyDB()
            } catch (e: Exception) {
                throw Error("Error copying DB")
            }
        }
    }

    @Throws(IOException::class)
    private fun copyDB() {
        val dbInput = mContext.assets.open(DB_NAME)
        val outFile = DB_PATH + DB_NAME
        val dbOutput: OutputStream = FileOutputStream(outFile)
        val buffer = ByteArray(1024)
        var length: Int
        while (dbInput.read(buffer).also { length = it } > 0) {
            dbOutput.write(buffer, 0, length)
        }
        dbOutput.flush()
        dbOutput.close()
        dbInput.close()
    }

    private fun checkDB(): Boolean {
        var check: SQLiteDatabase? = null
        try {
            val dbPath = DB_PATH + DB_NAME
            check = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
        } catch (e: Exception) {
            // TODO: handle exception
        }
        check?.close()
        return if (check != null) true else false
    }

    //    public void openDB(){
    //        String dbPath = DB_PATH+DB_NAME;
    //        db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    //    }
    @Synchronized
    override fun close() {
        if (db != null) db!!.close()
        super.close()
    }

//    fun getValues(idHexegram: String): Hexegram? {
//        val dataxam = Hexegram()
//        val query = "SELECT * FROM ft_hexagram where h_ID ='$idHexegram'"
//        db = this.writableDatabase
//        val coCursor = db!!.rawQuery(query, null)
//        coCursor.moveToFirst()
//        dataxam.h_ID = coCursor.getString(0)
//        dataxam.number = coCursor.getInt(1)
//        dataxam.h_name = coCursor.getString(2)
//        dataxam.h_mean = coCursor.getString(3)
//        dataxam.h_description = coCursor.getString(4)
//        dataxam.h_content = coCursor.getString(5)
//        dataxam.h_wao1 = coCursor.getString(6)
//        dataxam.h_wao2 = coCursor.getString(7)
//        dataxam.h_wao3 = coCursor.getString(8)
//        dataxam.h_wao4 = coCursor.getString(9)
//        dataxam.h_wao5 = coCursor.getString(10)
//        dataxam.h_wao6 = coCursor.getString(11)
//        return dataxam
//    }

    fun getAllValue(): List<Hexagram>? {
        var listHexagram: ArrayList<Hexagram> = ArrayList()
        val query = "SELECT * FROM ft_hexagram"
        db = this.writableDatabase
        val coCursor = db!!.rawQuery(query, null)
        if (coCursor.moveToFirst()) {
            while (!coCursor.isAfterLast()) {
                var hID = coCursor.getString(0)
                var number = coCursor.getString(1)
                var hName = coCursor.getString(2)
                var hMean = coCursor.getString(3)
                var hDescription = coCursor.getString(4)
                var hContent = coCursor.getString(5)
                var hWao1 = coCursor.getString(6)
                var hWao2 = coCursor.getString(7)
                var hWao3 = coCursor.getString(8)
                var hWao4 = coCursor.getString(9)
                var hWao5 = coCursor.getString(10)
                var hWao6 = coCursor.getString(11)
                listHexagram.add(Hexagram(hID, number, hName, hMean, hDescription, hContent, hWao1, hWao2, hWao3, hWao4, hWao5, hWao6))
                coCursor.moveToNext()
            }
        }
        return listHexagram
    }

    companion object {
        var DB_PATH = "data/data/com.cannshine.fortune/databases/"
        var DB_NAME = "FortuneDB.sqlite"
    }
}