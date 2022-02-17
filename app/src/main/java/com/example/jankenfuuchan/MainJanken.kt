package com.example.jankenfuuchan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.jankenfuuchan.databinding.ActivityMainJankenBinding
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main_janken.*

class MainJanken : AppCompatActivity() {

    private lateinit var binding : ActivityMainJankenBinding
    private lateinit var realm : Realm

    //プレイヤークラスの画像バイト配列をビットマップに変換して格納するクラス
    inner class PlayerBitmap(player: Player) {
        val id: Long
        val name: String
        val bitmapReady: Bitmap
        val bitmapGuu: Bitmap
        val bitmapChoki: Bitmap
        val bitmapPaa: Bitmap
        val bitmapWin: Bitmap
        val bitmapLose: Bitmap

        init {
            this.id = player.id
            this.name = player.name
            this.bitmapReady = MyUtils.getImageFromByte(player.byteReady)
            this.bitmapGuu = MyUtils.getImageFromByte(player.byteGuu)
            this.bitmapChoki = MyUtils.getImageFromByte(player.byteChoki)
            this.bitmapPaa = MyUtils.getImageFromByte(player.bytePaa)
            this.bitmapWin = MyUtils.getImageFromByte(player.byteWin)
            this.bitmapLose = MyUtils.getImageFromByte(player.byteLose)
        }
    }
        private lateinit var player1 : PlayerBitmap
        private lateinit var player2 : PlayerBitmap
        private lateinit var listPlayer : MutableList<PlayerBitmap>

        private var gameFlag : Int = 0 //じゃんけんの状態を格納
        private val JANKEN = 0 //じゃんけん・・・・
        private val AIKO = 1 //あいこで・・・・
        private val RESULT = 2 //結果

        private val GUU = 0
        private val CHOKI = 1
        private val PAA = 2

        private var winCount1 = 0 //プレイヤー1の勝利数
        private var winCount2 = 0 //プレイヤー2の勝利数
        private  var gameCount = 0 //試合カウント
        private var maxGameCount = 0 //最大試合数

        private val arrayReadySerifu = arrayOf("まけないぞ～", "なにをだそう？", "う～ん")
        private val arrayWinSerifu = arrayOf("やった～！", "なにをだそう？", "うれしいな！")
        private val arrayLoseSerifu = arrayOf("くやしい～", "まけた～", "かなしい・・")

        private var mediaPlayer : MediaPlayer? = null //BGM再生
        private lateinit var soundPlayer: SoundPlayer //ボイス再生


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainJankenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        realm = Realm.getDefaultInstance()
        soundPlayer = SoundPlayer(this)

        init()
    }

    //初期化メソッド

    fun init(){

        //プレイヤーリストと名前リストをデータベースから取得

        val realmResults = realm.where<Player>().findAll()
        listPlayer = ArrayList()
        val listPlayerName = ArrayList<String>()
        var i = 0

        for(player in realmResults ){
            listPlayer.add(PlayerBitmap(player))
            listPlayerName.add((i + 1).toString() + " " + player.name)
            i++
        }


        //スピナーのアダプターとリスナー

        var spinnerAdapter = ArrayAdapter<String>(this, R.layout.spinner3, listPlayerName)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown)
        var spinnerListener: AdapterView.OnItemSelectedListener

        //プレイヤー１選択スピナー

        binding.spinnerPlayer1.adapter = spinnerAdapter

        spinnerListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                player1 = listPlayer[p2]
                binding.imagePlayer1.setImageBitmap(player1.bitmapReady)
                binding.textComent.text = player1.name + "　と\n" + player2.name + "　の\n" + "じゃんけん！"
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        binding.spinnerPlayer1.onItemSelectedListener = spinnerListener

        //プレイヤー2選択スピナー

        binding.spinnerPlayer2.adapter = spinnerAdapter
        spinnerListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                player2 = listPlayer[p2]
                binding.imagePlayer2.setImageBitmap(player2.bitmapReady)
                binding.textComent.text = player1.name + "　と\n" + player2.name + "　の\n" + "じゃんけん！"
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        binding.spinnerPlayer2.onItemSelectedListener = spinnerListener

        //ゲーム回数スピナー

        spinnerAdapter = ArrayAdapter<String>(
            this, R.layout.spinner2, arrayOf(
                "1",
                "3",
                "5",
                "7",
                "10",
                "15",
                "20"
            )
        )
            binding.spinnerMaxTimes.adapter = spinnerAdapter
        spinnerListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                maxGameCount = p0?.selectedItem.toString().toInt()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        binding.spinnerMaxTimes.onItemSelectedListener = spinnerListener

        //ＢＧＭスピナー

        spinnerAdapter = ArrayAdapter(
            this, R.layout.spinner3, arrayOf(
                "１　♪",
                "２　♪",
                "３　♪",
                "４　♪",
                "５　♪",
                "♪なし"
            )
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown)
        binding.spinnerBGM.adapter = spinnerAdapter
        spinnerListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                mediaPlayer?.stop()
                when(p2){
                    0 -> {
                        mediaPlayer = MediaPlayer.create(this@MainJanken, R.raw.bgm1).apply {
                            isLooping = true
                            start()
                        }
                    }
                    1 -> {
                        mediaPlayer = MediaPlayer.create(this@MainJanken, R.raw.bgm2).apply {
                            isLooping = true
                            start()
                        }
                    }
                    2 -> {
                        mediaPlayer = MediaPlayer.create(this@MainJanken, R.raw.bgm3).apply {
                            isLooping = true
                            start()
                        }
                    }
                    3 -> {
                        mediaPlayer = MediaPlayer.create(this@MainJanken, R.raw.bgm4).apply {
                            isLooping = true
                            start()
                        }
                    }
                    4 -> {
                        mediaPlayer = MediaPlayer.create(this@MainJanken, R.raw.bgm5).apply {
                            isLooping = true
                            start()
                        }
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        binding.spinnerBGM.onItemSelectedListener = spinnerListener

        reset()
    }

    //リセット処理

    fun reset(){
        binding.apply {

            player1 = listPlayer[0]
            player2 = listPlayer[1]
            imagePlayer1.setImageBitmap(player1.bitmapReady)
            imagePlayer2.setImageBitmap(player2.bitmapReady)

            spinnerPlayer1.setSelection(0)
            spinnerPlayer2.setSelection(1)
            spinnerMaxTimes.setSelection(0)
            spinnerPlayer1.isEnabled = true
            spinnerPlayer2.isEnabled = true
            spinnerMaxTimes.isEnabled = true

            textJanken1.text = ""
            textJanken2.text = ""
            textWinFrag1.setBackgroundResource(android.R.drawable.btn_star_big_off)
            textWinFlag2.setBackgroundResource(android.R.drawable.btn_star_big_off)

            winCount1 = 0
            winCount2 = 0
            textWinTimes1.text = "0"
            textWinTimes2.text = "0"

            gameFlag = JANKEN

            gameCount = 0
            textGameTimes.text = "$gameCount かいめ"

            textComent.text = player1.name + "　と\n" + player2.name + "　の\n" + "じゃんけん！"

            buttonGuu.isEnabled = false
            buttonChoki.isEnabled = false
            buttonPaa.isEnabled = false
            buttonStart.isEnabled = true
            buttonStart.text = "じゃんけん"
            buttonStart.setBackgroundResource(R.drawable.bottontype)
        }
    }
    //リセットボタン

    fun onButtonReset(view: View){
        reset()
    }

    //じゃんけん、あいこ、けっかボタン

    fun onButtonStart(view : View){

        binding.apply {
            spinnerPlayer1.isEnabled = false
            spinnerPlayer2.isEnabled = false
            spinnerMaxTimes.isEnabled = false
            buttonStart.setBackgroundResource(R.drawable.bottontype2)

            if(gameFlag == AIKO){
                ready()
                binding.textComent.text = "あいこで・・・"
                soundPlayer.playSound(SoundPlayer.voiceAIKO)
            }
            else if(gameFlag == JANKEN){
                ready()
                binding.textComent.text = "じゃんけん・・・"
                gameCount++
                textGameTimes.text = "$gameCount　かいめ"
                soundPlayer.playSound(SoundPlayer.voiceJANKEN)
            }
            else {// gameFlag == RESULT
                //最終勝利判定
                binding.apply {
                    buttonStart.isEnabled = false
                    val winCount1 = textWinTimes1.text.toString().toInt()
                    val winCount2 = textWinTimes2.text.toString().toInt()

                    if(winCount1 == winCount2){
                        textComent.text = "$winCount1　たい　$winCount2　で\nひきわけ～"
                    }
                    else if (winCount1 > winCount2){
                        soundPlayer.playSound(SoundPlayer.voiceWIN)
                        textComent.text = "$winCount1　たい　$winCount2　で\n${player1.name}の\nかち～！"
                        textJanken1.text = arrayWinSerifu[(Math.random() * 3).toInt()]
                        textJanken2.text = arrayLoseSerifu[(Math.random() * 3).toInt()]
                        imagePlayer1.setImageBitmap(player1.bitmapWin)
                        imagePlayer2.setImageBitmap(player2.bitmapLose)
                    }
                    else if (winCount1 < winCount2){
                        soundPlayer.playSound(SoundPlayer.voiceLOSE)
                        textComent.text = "$winCount1　たい　$winCount2　で\n${player2.name}の\nかち～！"
                        textJanken1.text = arrayLoseSerifu[(Math.random() * 3).toInt()]
                        textJanken2.text = arrayWinSerifu[(Math.random() * 3).toInt()]
                        imagePlayer1.setImageBitmap(player1.bitmapLose)
                        imagePlayer2.setImageBitmap(player2.bitmapWin)
                    }
                }
            }
        }
    }

    //ready()メソッド

    fun ready(){
        binding.apply {
            buttonGuu.isEnabled = true
            buttonChoki.isEnabled = true
            buttonPaa.isEnabled = true
            buttonStart.isEnabled = false
            imagePlayer1.setImageBitmap(player1.bitmapReady)
            imagePlayer2.setImageBitmap(player2.bitmapReady)
            textJanken1.text = arrayReadySerifu[(Math.random() * 3).toInt()]
            textJanken2.text = arrayReadySerifu[(Math.random() * 3).toInt()]
            textWinFrag1.setBackgroundResource(android.R.drawable.btn_star_big_off)
            textWinFlag2.setBackgroundResource(android.R.drawable.btn_star_big_off)
        }
    }
    //ぐー、ちょき、ぱーボタン

    fun onButtonPaa(view : View){
        binding.imagePlayer1.setImageBitmap(player1.bitmapPaa)
        binding.textJanken1.text = "パー"
        janken()
        gameResult(PAA)
    }
    fun onButtonChoki(view : View){
        binding.imagePlayer1.setImageBitmap(player1.bitmapChoki)
        binding.textJanken1.text = "チョキ"
        janken()
        gameResult(CHOKI)
    }
    fun onButtonGuu(view : View){
        binding.imagePlayer1.setImageBitmap(player1.bitmapGuu)
        binding.textJanken1.text = "グー"
        janken()
        gameResult(GUU)
    }

    //じゃんけんボタンの共通処理

    fun janken(){

        binding.apply {
            buttonStart.setBackgroundResource(R.drawable.bottontype)
            buttonStart.isEnabled = true
            buttonPaa.isEnabled = false
            buttonGuu.isEnabled = false
            buttonChoki.isEnabled = false
            if (gameFlag == AIKO){
                textComent.text = "し　ょ　！"
                soundPlayer.playSound(SoundPlayer.voiceSHO)
            }
            else { //gameFlag == JANKEN
                textComent.text = "ぽ　ん　！"
                soundPlayer.playSound(SoundPlayer.voicePON)
            }
        }
    }

    //じゃんけん勝敗決定///////////////////////

    fun gameResult(myHand : Int){

        binding.apply {

            //プレイヤー２（コンピューター）の手を決める
            val comHand = (Math.random() * 3).toInt()
            when(comHand){
                GUU -> {
                    imagePlayer2.setImageBitmap(player2.bitmapGuu)
                    textJanken2.text = "グー"
                }
                CHOKI -> {
                    imagePlayer2.setImageBitmap(player2.bitmapChoki)
                    textJanken2.text = "チョキ"
                }
                PAA -> {
                    imagePlayer2.setImageBitmap(player2.bitmapPaa)
                    textJanken2.text = "パー"
                }
            }
            //勝敗判定
            val gameResult = (comHand - myHand + 3) % 3

            when (gameResult){
                0 -> { //あいこ
                    gameFlag = AIKO
                    buttonStart.text = "あいこ"
                }
                1 -> { //勝ち
                    gameFlag = JANKEN
                    buttonStart.text = "じゃんけん"
                    winCount1++
                    textWinFrag1.setBackgroundResource(android.R.drawable.btn_star_big_on)
                }
                2 -> { //負け
                    gameFlag = JANKEN
                    buttonStart.text = "じゃんけん"
                    winCount2++
                    textWinFlag2.setBackgroundResource(android.R.drawable.btn_star_big_on)
                }
            }
            //勝ち数表示
            textWinTimes1.text = "$winCount1"
            textWinTimes2.text = "$winCount2"
            //試合終了判定
            if(gameCount >= maxGameCount && gameFlag != AIKO){
                gameFlag = RESULT
                buttonStart.text = "けっか"
            }
        }
    }
    //もどる、おわるボタン
    fun onButtonFinish(view : View){
        finish()
    }
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
        mediaPlayer?.run {
            if (isPlaying()) {
                stop()
            }
            release()
            mediaPlayer = null
        }
    }
}
