package com.josetotana.totanabusiness.itemCard


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.transition.Slide
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.josetotana.totanabusiness.R
import com.josetotana.totanabusiness.categoryItems.CategoryItemModel


class ItemCardActivity : AppCompatActivity() {

    private lateinit var categoryItem: CategoryItemModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)

            // set an exit transition
            enterTransition = Slide()
            exitTransition = Slide()
        }
        setContentView(R.layout.activity_item_card)

        categoryItem = intent.getSerializableExtra("CategoryModel") as CategoryItemModel
        populateCard(categoryItem)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun populateCard(categoryItem: CategoryItemModel) {

        val cardName = findViewById<TextInputEditText>(R.id.cardName)
        cardName.setText(categoryItem.name)

        val cardAddress = findViewById<TextInputEditText>(R.id.cardAddress)
        cardAddress.setText(if (categoryItem.address == "null") " " else categoryItem.address)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        var height = displayMetrics.heightPixels

        cardAddress.maxWidth = (width*0.7).toInt()

        val mapsButton = findViewById<Button>(R.id.mapsButton)
        mapsButton.setOnClickListener(addressClickListener)

        val favIcon = findViewById<ImageView>(R.id.favIcon)
        val sharedPref = getSharedPreferences(
            resources.getString(R.string.fav_storage_file_name),
            Context.MODE_PRIVATE
        ) ?: return

        categoryItem.isFav = sharedPref.getBoolean(categoryItem.id, false)
        if (categoryItem.isFav) favIcon.setImageResource(R.drawable.star) else favIcon.setImageResource(
            R.drawable.star_border
        )
        favIcon.setOnClickListener(FavIconClickListener(categoryItem, sharedPref))

        val cardPhone = findViewById<TextInputEditText>(R.id.cardPhone)
        cardPhone.setText(if (categoryItem.phone == "null") " " else categoryItem.phone)
        cardPhone.setOnClickListener(phoneClickListener)

        val cardWhatsapp = findViewById<TextInputEditText>(R.id.cardWhatsapp)
        cardWhatsapp.setText(categoryItem.whatsapp)
        cardWhatsapp.setText(if (categoryItem.whatsapp == "null") " " else categoryItem.whatsapp)
        cardWhatsapp.setOnClickListener(whatsappClickListener)

        if (categoryItem.imageURL.isNotEmpty()) {
            val cardImage = findViewById<ImageView>(R.id.cardImage)

            Glide.with(this /* context */)
                .load(categoryItem.imageURL)
                .into(cardImage)

        }

    }

    inner class FavIconClickListener(
        private var categoryItem: CategoryItemModel,
        private val sharedPref: SharedPreferences
    ) : View.OnClickListener {
        override fun onClick(v: View?) {
            if (categoryItem.isFav) {
                (v as ImageView).setImageResource(R.drawable.star_border)
                sharedPrefSetValue(false, categoryItem.id, sharedPref)
                categoryItem.isFav = false

            } else {
                (v as ImageView).setImageResource(R.drawable.star)
                categoryItem.isFav = true
//                val gson = Gson()
//                val json = gson.toJson(categoryItem)
                sharedPrefSetValue(true, categoryItem.id, sharedPref)
            }
        }

        private fun sharedPrefSetValue(
            value: Boolean,
            id: String,
            sharedPref: SharedPreferences
        ) {
            with(sharedPref.edit()) {
                if (value) putBoolean(id, value) else remove(id)
                commit()
            }

        }
    }

    private val phoneClickListener = View.OnClickListener { view ->
        if ((view as TextInputEditText).text != null && view.text?.trim()
                ?.isNotEmpty()!!
        ) {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:" + view.text)
            )
            startActivity(intent)

        }
    }

    private val whatsappClickListener = View.OnClickListener { view ->

        if ((view as TextInputEditText).text != null && view.text?.trim()
                ?.isNotEmpty()!!
        ) {

            val url = "https://api.whatsapp.com/send?phone=$" + view.text
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }

    private val addressClickListener = View.OnClickListener { view ->

        if (categoryItem.maps.isNotEmpty() && categoryItem.maps != "null") {

            val uri =
                categoryItem.maps
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }
    }

}