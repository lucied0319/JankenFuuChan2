package com.example.jankenfuuchan

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.text.FieldPosition

//保存確認ダイアログ

class AlertSaveDialog : DialogFragment(){

    interface  OnAlertSaveListener{
        fun onSaveClick()
    }
    private lateinit var listener : OnAlertSaveListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when{
            context is OnAlertSaveListener -> listener = context
            else -> {}
        }
    }
    companion object{

        fun newInstance(viewId : Int,dataString : String) =
            AlertSaveDialog().apply {
                arguments = Bundle().apply {
                    putInt("viewId",viewId)
                    putString("dataString",dataString)
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewId = arguments?.getInt("viewId")
        val dataString = arguments?.getString("dataString")
        val message =
            if (viewId==R.id.buttonNameSave){"「$dataString」のなまえでとうろくしていいですか？"} //名前登録ボタン
            else if (viewId == R.id.buttonImageSave){"うえのがぞうを${dataString}にとうろくしていいですか？"} //画像登録ボタン
            else{"エラー"}

        AlertDialog.Builder(activity).apply {
            setTitle("とうろくかくにん")
            setMessage(message)
            setPositiveButton("OK"){dialog,which ->
                listener.onSaveClick()
            }
            setNegativeButton("キャンセル"){dialog,which->
            }
            return create()
        }
    }
}
//削除確認ダイアログ

class AlertDeleteDialog : DialogFragment(){

    interface OnAlertDeleteListener{
        fun onDeleteClick(position: Int)
    }
    private lateinit var listener : OnAlertDeleteListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when{
            context is OnAlertDeleteListener ->listener = context
            else ->{}
        }
    }
    companion object{

        fun newInstance(position: Int,name : String) =
            AlertDeleteDialog().apply {
                arguments = Bundle().apply {
                    putString("name",name)
                    putInt("position",position)
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val name = arguments?.getString("name")
        val position = arguments?.getInt("position")
        return AlertDialog.Builder(activity).apply {
            setTitle("さくじょかくにん")
            setMessage("「$name」 のデータをけしてもいいですか？")
            setPositiveButton("OK"){dialog,which->
                if (position != null)
                listener.onDeleteClick(position)
            }
            setNegativeButton("キャンセル"){dialog,which ->{}}
        }.create()
    }
}