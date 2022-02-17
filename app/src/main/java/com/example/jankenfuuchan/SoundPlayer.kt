package com.example.jankenfuuchan

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build

class SoundPlayer(val context : Context) {
    private var soundPool : SoundPool
    private lateinit var audioAttributes: AudioAttributes
    var voiceArray : IntArray = IntArray(6)

    init {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(2)
                .build()
        }else{
            soundPool = SoundPool(2,AudioManager.STREAM_MUSIC,0)
        }

        voiceArray[voiceJANKEN] = soundPool.load(context,R.raw.janken,1)
        voiceArray[voicePON] = soundPool.load(context,R.raw.pon,1)
        voiceArray[voiceAIKO] = soundPool.load(context,R.raw.aiko4,1)
        voiceArray[voiceSHO] = soundPool.load(context,R.raw.syo,1)
        voiceArray[voiceWIN] = soundPool.load(context,R.raw.win,1)
        voiceArray[voiceLOSE] = soundPool.load(context,R.raw.lose,1)
    }

    companion object{
        val voiceJANKEN = 0
        val voicePON = 1
        val voiceAIKO = 2
        val voiceSHO = 3
        val voiceWIN = 4
        val voiceLOSE = 5
    }
    fun playSound(i:Int){
        soundPool.play(voiceArray[i],1.0f,1.0f,1,0,1.0f)
    }



}