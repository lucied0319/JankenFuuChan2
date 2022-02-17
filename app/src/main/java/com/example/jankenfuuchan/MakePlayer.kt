package com.example.jankenfuuchan

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jankenfuuchan.databinding.ActivityMakePlayerBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_make_player.*
import java.text.SimpleDateFormat
import java.util.*

class MakePlayer : AppCompatActivity() ,AlertDeleteDialog.OnAlertDeleteListener ,AlertSaveDialog.OnAlertSaveListener {

    private lateinit var binding : ActivityMakePlayerBinding
    private lateinit var realm : Realm

    private lateinit var arrayButtonJanken : Array<Button>
    private lateinit var player: Player

    //列挙型
    enum class ImageStatus(val status : String){
        READY("「じゅんび」"),GUU("「グー」"),CHOKI("「チョキ」"),PAA("「パー」"),WIN("「かち」"),LOSE("「まけ」")
    }
    private var imageStatus : ImageStatus = ImageStatus.READY

    private var uriCamera : Uri? = null
    private var viewId : Int = -1

    //onRequestPermissionsResultが非推奨のためパーミッション許可の結果を受け取るインスタンスを作成

    val requestPermissonsResult = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){ //許可ＯＫ
            when{
                viewId == R.id.buttonPhoto ->{
                   onPhotoClick()
                }
                viewId == R.id.buttonCamera ->{
                    onCameraClick()
                }
            }
        }
    }

    //onActivityResultが非推奨のため画像取込ボタンの結果を受け取るインスタンスを作成

    val resultImageLancher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it?.resultCode == Activity.RESULT_OK){
            val uri = it.data?.data
            binding.imagePhoto.setImageURI(uri)
        }
    }

    //onActivityResultが非推奨のためカメラボタンの結果を受け取るインスタンスを作成

    val resultCameraLancher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it?.resultCode == Activity.RESULT_OK){

            binding.imagePhoto.setImageURI(uriCamera)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
        binding = ActivityMakePlayerBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.apply {
            layoutMain3.background.alpha = 120
            linearLeft.background.alpha = 200
            arrayButtonJanken = arrayOf(buttonReady,buttonGuu,buttonChoki,buttonPaa,buttonWin,buttonLose)

            //最初の画面初期化

            init()

            //名前エディットのリスナーをセット　※一番下に別の方法を表記

            editName.addTextChangedListener(
                object : TextWatcher{
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }
                    override fun afterTextChanged(editable: Editable?) {
                        editable?.run{
                            if (this.toString().length == 0) {
                                buttonNameSave.setBackgroundResource(R.drawable.bottontype2)
                                buttonNameSave.isEnabled = false
                            } else{
                                buttonNameSave.setBackgroundResource(R.drawable.bottontype)
                                buttonNameSave.isEnabled = true
                            }
                        }
                    }
                }
            )

            //////////////////////////////////////ボタンのリスナーをセット//////////////////////////

            //じゃんけんボタン

            for (i in 0..5){
                //各ボタンにリスナーを設定
                arrayButtonJanken[i].setOnClickListener {
                    for(d in 0..5){
                        arrayButtonJanken[d].setBackgroundResource(R.drawable.bottontype2)
                    }
                    it.setBackgroundResource(R.drawable.bottontype)
                    when(it.id){
                        R.id.buttonReady -> {
                            imagePhoto.setImageBitmap(MyUtils.getImageFromByte(player.byteReady))
                            imageStatus = ImageStatus.READY
                        }
                        R.id.buttonGuu -> {
                            imagePhoto.setImageBitmap(MyUtils.getImageFromByte(player.byteGuu))
                            imageStatus = ImageStatus.GUU
                        }
                        R.id.buttonChoki -> {
                            imagePhoto.setImageBitmap(MyUtils.getImageFromByte(player.byteChoki))
                            imageStatus = ImageStatus.CHOKI
                        }
                        R.id.buttonPaa -> {
                            imagePhoto.setImageBitmap(MyUtils.getImageFromByte(player.bytePaa))
                            imageStatus = ImageStatus.PAA
                        }
                        R.id.buttonWin -> {
                            imagePhoto.setImageBitmap(MyUtils.getImageFromByte(player.byteWin))
                            imageStatus = ImageStatus.WIN
                        }
                        R.id.buttonLose -> {
                            imagePhoto.setImageBitmap(MyUtils.getImageFromByte(player.byteLose))
                            imageStatus = ImageStatus.LOSE
                        }
                    }
                }
            }

            //名前登録ボタン

            buttonNameSave.setOnClickListener{
                viewId = it.id
                val dialog = AlertSaveDialog.newInstance(viewId,binding.editName.text.toString())
                dialog.show(supportFragmentManager,"保存")
            }

            //画像登録ボタン

            buttonImageSave.setOnClickListener{
                viewId = it.id
                val dialog = AlertSaveDialog.newInstance(viewId,imageStatus.status)
                dialog.show(supportFragmentManager,"保存")
            }

            //画像取込ボタン

            buttonPhoto.setOnClickListener{
                viewId = it.id
                requestWriteStorage()
            }

            //カメラボタン

            buttonCamera.setOnClickListener{
                viewId = it.id
                requestWriteStorage()
            }

            //終了ボタン

            buttonFinish.setOnClickListener{
                finish()
            }
        }
    }

    //初期化処理

    fun init(){

        for(d in 0..5){
            arrayButtonJanken[d].setBackgroundResource(R.drawable.bottontype2)
        }
        arrayButtonJanken[0].setBackgroundResource(R.drawable.bottontype)
        val realmResults = realm.where<Player>().findAll()
        player = realmResults.first()!!
        binding.imagePhoto.setImageBitmap(MyUtils.getImageFromByte(player.byteReady))
        binding.textName.text = "1　ワンワン"
        imageStatus = ImageStatus.READY

        //リサイクルビューのオブジェクトを作成とリスナーの設定

        val linearLayoutManager =  LinearLayoutManager(this@MakePlayer)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recycleView.layoutManager = linearLayoutManager
        val adapter = PlayerRealmAdapter(realmResults,true)
        binding.recycleView.adapter = adapter
    }

    //リサイクルビューのタップ処理

    fun onRecycleViewClick(position : Int,playerData : Player){

        binding.apply {
            player = playerData
            imagePhoto.setImageBitmap(MyUtils.getImageFromByte(player.byteReady))
            imageStatus = ImageStatus.READY
            textName.text = "${position + 1}　${player.name}"
            for(i in 0..5){
                arrayButtonJanken[i].setBackgroundResource(R.drawable.bottontype2)
            }
            arrayButtonJanken[0].setBackgroundResource(R.drawable.bottontype)

            //ワンワンとうーたんの選択時は保存もカメラ使わせない

            if(position == 0 || position == 1){
                buttonImageSave.setBackgroundResource(R.drawable.bottontype2)
                buttonImageSave.isEnabled = false
                buttonCamera.setBackgroundResource(R.drawable.bottontype2)
                buttonCamera.isEnabled = false
                buttonPhoto.setBackgroundResource(R.drawable.bottontype2)
                buttonPhoto.isEnabled = false
            }
            else{
                buttonImageSave.setBackgroundResource(R.drawable.bottontype)
                buttonImageSave.isEnabled = true
                buttonCamera.setBackgroundResource(R.drawable.bottontype)
                buttonCamera.isEnabled = true
                buttonPhoto.setBackgroundResource(R.drawable.bottontype)
                buttonPhoto.isEnabled = true
            }
        }
    }

    //ストレージパーミッション許可確認

    fun requestWriteStorage(){

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED){
            requestPermissonsResult.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        else{
            when{
                viewId == R.id.buttonPhoto ->{
                   onPhotoClick()
                }
                viewId == R.id.buttonCamera ->{
                   onCameraClick()
                }
            }
        }
    }

    //画像取込処理

    fun onPhotoClick(){

        val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*")
        resultImageLancher.launch(intent)
    }

    //カメラ処理

    fun onCameraClick(){

        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val now = Date(System.currentTimeMillis())
        val nowStr = dateFormat.format(now)
        val fileName = "Fuu$nowStr.png"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,fileName)
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/png")
        val resolver = contentResolver
        uriCamera = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uriCamera)
        resultCameraLancher.launch(intent)
    }

    //保存処理

    override fun onSaveClick() {

        //名前登録ボタン

        if (viewId == R.id.buttonNameSave){
            val realmResults = realm.where<Player>().findAll()
            val size = realmResults.size
            if (size >= 15){
                Snackbar.make(binding.root,"とうろくは15人までです",Snackbar.LENGTH_SHORT).show()
            }
            else{
                realm.executeTransaction{
                    val maxId = realm.where<Player>().max("id")
                    var id : Long
                    maxId?.let {
                        id = it.toLong() + 1
                        val player = realm.createObject<Player>(id)
                        player.apply {
                            player.name = binding.editName.text.toString()
                            var bitmap = BitmapFactory.decodeResource(resources,R.drawable.himaready)
                            player.byteReady = MyUtils.getByteFromImage(bitmap)
                            bitmap = BitmapFactory.decodeResource(resources,R.drawable.himaguu)
                            player.byteGuu = MyUtils.getByteFromImage(bitmap)
                            bitmap = BitmapFactory.decodeResource(resources,R.drawable.himachoki)
                            player.byteChoki = MyUtils.getByteFromImage(bitmap)
                            bitmap = BitmapFactory.decodeResource(resources,R.drawable.himapaa)
                            player.bytePaa = MyUtils.getByteFromImage(bitmap)
                            bitmap = BitmapFactory.decodeResource(resources,R.drawable.himawin)
                            player.byteWin = MyUtils.getByteFromImage(bitmap)
                            bitmap = BitmapFactory.decodeResource(resources,R.drawable.himalose)
                            player.byteLose = MyUtils.getByteFromImage(bitmap)
                        }
                        binding.editName.setText("")
                        Snackbar.make(binding.root,"とうろくしました",Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        //画像登録ボタン

        else if (viewId == R.id.buttonImageSave){

            realm.executeTransaction{
                val bitamapDrawable = imagePhoto.drawable as BitmapDrawable
                when(imageStatus){
                    ImageStatus.READY -> player.byteReady = MyUtils.getByteFromImage(bitamapDrawable.bitmap)
                    ImageStatus.GUU -> player.byteGuu = MyUtils.getByteFromImage(bitamapDrawable.bitmap)
                    ImageStatus.CHOKI -> player.byteChoki = MyUtils.getByteFromImage(bitamapDrawable.bitmap)
                    ImageStatus.PAA -> player.bytePaa = MyUtils.getByteFromImage(bitamapDrawable.bitmap)
                    ImageStatus.WIN -> player.byteWin = MyUtils.getByteFromImage(bitamapDrawable.bitmap)
                    ImageStatus.LOSE -> player.byteLose = MyUtils.getByteFromImage(bitamapDrawable.bitmap)
                }
                Snackbar.make(binding.root,"とうろくしました",Snackbar.LENGTH_SHORT).show()

            }
        }
    }

    //削除処理

    override fun onDeleteClick(position: Int) {

        if(position == 0 || position == 1)
            Snackbar.make(binding.root,"「ワンワン」と「うーたん」はけせないよ！",Snackbar.LENGTH_SHORT).show()
        else{
            realm.executeTransaction{
                realm.where<Player>().findAll().deleteFromRealm(position)
                Snackbar.make(binding.root,"データをけしました",Snackbar.LENGTH_SHORT).show()
            }
            init()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }



}
/////////////////////////////この方法だとTextWatcherのリスナーを単体で登録可能////////////////////////////////

//            editName.doAfterTextChanged{
//                it?.run{
//                    if (this.toString().length == 0) {
//                        buttonNameSave.setBackgroundResource(R.drawable.bottontype2)
//                        buttonNameSave.isEnabled = false
//                    } else{
//                        buttonNameSave.setBackgroundResource(R.drawable.bottontype)
//                        buttonNameSave.isEnabled = true
//                    }
//                }
//            }