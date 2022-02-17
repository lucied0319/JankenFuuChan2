package com.example.jankenfuuchan

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Player : RealmObject(){
    @PrimaryKey
    var id : Long = 0
    var name : String = ""
    var byteReady : ByteArray? = null
    var byteGuu : ByteArray? = null
    var byteChoki : ByteArray? = null
    var bytePaa : ByteArray? = null
    var byteWin : ByteArray? = null
    var byteLose : ByteArray? = null
}