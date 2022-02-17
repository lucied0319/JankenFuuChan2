package com.example.jankenfuuchan

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.jankenfuuchan.databinding.ActivityMainBinding
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where


class MainActivity : AppCompatActivity() {

    private  lateinit var  binding : ActivityMainBinding
    private lateinit var realm : Realm


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.layoutMain2.background.alpha = 150
        setContentView(binding.root)

        realm = Realm.getDefaultInstance()

        realm.executeTransaction{
            var i : Int
            var bitmap : Bitmap
            var player : Player?

            //ワンワン

            i = 0
            player = realm.where<Player>().equalTo("id", i).findFirst()

            //ワンワンのデータがなければ作成

            if(player == null) {
                player = realm.createObject<Player>(i)
            }
            player.apply{
                name = "ワンワン"
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.wanwanready)
                byteReady = MyUtils.getByteFromImage(bitmap)
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.wanwanguu)
                byteGuu = MyUtils.getByteFromImage(bitmap)
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.wanwanchoki)
                byteChoki = MyUtils.getByteFromImage(bitmap)
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.wanwanper2)
                bytePaa = MyUtils.getByteFromImage(bitmap)
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.wanwanwin)
                byteWin = MyUtils.getByteFromImage(bitmap)
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.wanwanlose)
                byteLose = MyUtils.getByteFromImage(bitmap)
            }

            //うーたん

            i = 1
            player = realm.where<Player>().equalTo("id", i).findFirst()

            //うーたんのデータがなければ作成

            if(player == null) {
                player = realm.createObject<Player>(i)
            }
            player.name = "うーたん"
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.utanready)
            player.byteReady = MyUtils.getByteFromImage(bitmap)
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.utanguu)
            player.byteGuu = MyUtils.getByteFromImage(bitmap)
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.utanchoki2)
            player.byteChoki = MyUtils.getByteFromImage(bitmap)
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.utanpaa2)
            player.bytePaa = MyUtils.getByteFromImage(bitmap)
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.utanwin)
            player.byteWin = MyUtils.getByteFromImage(bitmap)
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.utanlose)
            player.byteLose = MyUtils.getByteFromImage(bitmap)
        }

        binding.buttonStart.setOnClickListener {
            val intent = Intent(this,MainJanken::class.java)
            startActivity(intent)
        }
        binding.button3.setOnClickListener {
            val intent = Intent(this,MakePlayer::class.java)
            startActivity(intent)
        }
        binding.button4.setOnClickListener {
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}