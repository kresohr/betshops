package com.ikresimir.betshops.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.ikresimir.betshops.R
import java.lang.ref.WeakReference
import java.util.*

class LocationDetailsDialog(context: Context, onDismissListener: DialogInterface.OnDismissListener) {
    private var context = context
    private var onDismiss = onDismissListener

    fun showLocationDetailsDialog(lat: Double, lng: Double, name: String, address: String, city: String, county: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet)

        val closeButton: ImageView = dialog.findViewById(R.id.btnClose)
        val routeButton: TextView = dialog.findViewById(R.id.txtRoute)
        val txtName: TextView = dialog.findViewById(R.id.txtName)
        val txtAddress: TextView = dialog.findViewById(R.id.txtAddress)
        val txtCityCounty: TextView = dialog.findViewById(R.id.txtCityCounty)
        val txtOpenHours: TextView = dialog.findViewById(R.id.txtOpenHours)
        txtOpenHours.text = checkCurrentTime()

        txtName.text = name.trim()
        txtAddress.text = address
        txtCityCounty.text = city + " - " + county

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setDimAmount(0F)
        dialog.window?.setBackgroundDrawableResource(R.color.white)
        dialog.setOnDismissListener {
            onDismiss
        }
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        routeButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+lat+","+lng+"?q="+lat+","+lng+"+("+name.trim()+")"))
            context.startActivity(intent)
        }
    }

    // If the current time is between 08:00 - 16:00, the shops are open.
    private fun checkCurrentTime(): String{
        val currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentTime in 8..15){
            return context.getString(R.string.text_open_now)
        }
        else
            return context.getString(R.string.text_open_tomorrow)
    }
}