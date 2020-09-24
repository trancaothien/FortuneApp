package com.cannshine.fortune.mainmenu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.cannshine.fortune.AppApplication
import com.cannshine.fortune.VolleyRequest.ApplicationController
import com.cannshine.fortune.db.Database
import com.cannshine.fortune.model.Hexegram
import com.cannshine.fortune.utils.Global
import com.cannshine.fortune.utils.Utils
import java.util.HashMap

class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
    var txvTitle: String? = null
    var idHexe = ""
    var data = Hexegram()
    var dataHexegram = Database(AppApplication.application)
    fun createUser(success: (String) -> Unit, fail:() -> Unit){
        val isneedUpdate = Utils.getFlagToken(AppApplication.application)
        val needUpdate = isneedUpdate[Global.K_TOKEN]
        val country = AppApplication.application.resources.configuration.locale.country
        val deviceId = Utils.getDeviceId(AppApplication.application)
        val stringRequest: StringRequest = object : StringRequest(Method.POST, Global.URL_MAIN_CREATE_USER, Response.Listener { response ->
            success(response)
        }, Response.ErrorListener { fail() }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["apikey"] = Global.APIKEY
                //                params.put("Content-Type", "application/json");
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val body = "&act=newuser&os=android&deviceid=$deviceId&location=$country&appid=1"
                return body.toByteArray(charset("utf-8"))
            }
        }
        ApplicationController.getInstance(AppApplication.application)?.addToRequestQueue(stringRequest)
    }

//    fun setTitle(flag: String, iDHexegram: String){
//        val idDaoNguoc = reverseID(iDHexegram)
//        val flagHD = reverseID(flag)
//        var kyTu: String
//        var flag: String
//        for (i in 0 until idDaoNguoc.length) {
//            flag = flagHD[i].toString()
//            kyTu = idDaoNguoc[i].toString()
//            if (flag == "1") {
//                kyTu = if (kyTu == "0") {
//                    "1"
//                } else {
//                    "0"
//                }
//            }
//            idHexe = idHexe + kyTu
//        }
//        data = dataHexegram.getValues(idHexe)!!
//        val name = data.h_name
//        txvTitle = name
//    }

    fun reverseID(id: String?): String {
        return StringBuffer(id!!).reverse().toString()
    }
}