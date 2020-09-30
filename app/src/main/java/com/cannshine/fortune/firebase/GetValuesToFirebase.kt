package com.cannshine.fortune.firebase

import android.util.Log
import com.cannshine.fortune.db.Database
import com.cannshine.fortune.model.Hexagram
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GetValuesToFirebase(hexagramDB: Database) {
    val listHexagram = hexagramDB.getAllValue()
    private lateinit var mDatabase: DatabaseReference

    fun writeHexagram(){
        mDatabase = Firebase.database.reference.child("hexagram") // init realTimeDB
        var hexagram: Hexagram?
        var i = 0
        if (listHexagram != null) {
            while (i < listHexagram.size - 1){
                hexagram = listHexagram[i]
                hexagram.h_ID?.let { mDatabase.child(it).setValue(hexagram)}
                i ++
            }
        }
    }
}