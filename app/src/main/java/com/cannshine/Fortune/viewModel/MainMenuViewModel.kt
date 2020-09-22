package com.cannshine.Fortune.viewModel

import android.content.Context
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.cannshine.Fortune.db.Database
import com.cannshine.Fortune.model.Hexegram


class MainMenuViewModel {
    var iDHexegram: String? = null
    var idHexe: String? = null
    var mContext: Context? = null
    var dataHexegram: Database? = null
    var data: Hexegram = Hexegram()
    var txvTile: String? = null
    fun init(context: Context){
        mContext = context
        this.dataHexegram = Database(context)
    }

    fun setTitle(flag: String) {
        val idDaoNguoc = reverseID(iDHexegram)
        val flagHD = reverseID(flag)
        var kyTu: String
        var flag: String
        if (idDaoNguoc != null) {
            for (i in 0 until idDaoNguoc.length) {
                flag = flagHD?.get(i).toString()
                kyTu = idDaoNguoc[i].toString()
                if (flag == "1") {
                    kyTu = if (kyTu == "0") {
                        "1"
                    } else {
                        "0"
                    }
                }
                idHexe = idHexe + kyTu
            }
        }
        data = dataHexegram!!.getValues(idHexe!!)!!
        txvTile = data.h_name
    }

    //dao nguoc chuoi
    fun reverseID(id: String?): String? {
        return id?.let { StringBuffer(it).reverse().toString() }
    }
}