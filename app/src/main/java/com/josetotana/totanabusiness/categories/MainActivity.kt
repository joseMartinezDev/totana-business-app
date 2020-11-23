package com.josetotana.totanabusiness.categories

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.transition.Fade
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.josetotana.totanabusiness.R
import com.josetotana.totanabusiness.categoryItems.SelectedItemActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MyCategoryRecyclerViewAdapter.ItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MyCategoryRecyclerViewAdapter
    private val categoryList = mutableListOf<CategoryModel>()
    private lateinit var popupEditText: EditText
    private lateinit var popupWindow: PopupWindow
    private lateinit var imm: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)

            // set an exit transition
            enterTransition = Fade()
            exitTransition = Fade()
        }

        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.rvCategories)

        recyclerView.layoutManager = LinearLayoutManager(this)
        viewAdapter = MyCategoryRecyclerViewAdapter(categoryList)
        viewAdapter.setClickListener(this)
        recyclerView.adapter = viewAdapter
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)


        getCategories()

        val favButton = findViewById<Button>(R.id.favButton)
        favButton.setOnClickListener(favClickListener)

        val searchItemButton = findViewById<Button>(R.id.searchItemButton)
        searchItemButton.setOnClickListener(searchItemClickListener)

//        val acc = supportActionBar
//        val d = ResourcesCompat.getDrawable(getResources(), R.color.colorGreenApp, null);
//        acc?.setBackgroundDrawable(d)

//        supportActionBar?.setTitle(HtmlCompat.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.app_name) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY))

        pullToRefresh.setOnRefreshListener {

            categoryList.clear()
            viewAdapter.categoryFilterList.clear()
            getCategories(false)
            pullToRefresh.isRefreshing = false

        }
    }

    private fun showNoConnectionMessage() {
        Toast.makeText(this, R.string.noConectionMessage, Toast.LENGTH_LONG).show()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewAdapter.filter.filter(newText)
                return false
            }
        })

        return true
    }


    private val favClickListener = View.OnClickListener { view ->

        val intent = Intent(this, SelectedItemActivity::class.java).apply {
            putExtra("favourites", true); putExtra("label", "Favoritos")
        }

        startActivity(
            intent,
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    private val searchItemClickListener = View.OnClickListener { view ->

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View =
            inflater.inflate(R.layout.search_item_popup, LinearLayout(this), false)


        // create the popup window
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true // lets taps outside the popup also dismiss it

        popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(view, Gravity.TOP, 0, 0)
        val v: View = popupWindow.contentView

        val popupButton = v.findViewById<Button>(R.id.popupButton)
        popupButton.setOnClickListener(onPopupButtonClick)
        popupEditText = v.findViewById(R.id.popupText)
        popupEditText.requestFocus()

        popupEditText.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)

        popupEditText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                performSearch(v.windowToken)
                return@OnKeyListener true
            }
            false
        })

    }

    private fun performSearch(token: IBinder) {
        if (popupEditText.text.toString().isEmpty()) {
            Toast.makeText(this, R.string.emptySearchWarning, Toast.LENGTH_SHORT).show()
        } else {

            val intent = Intent(this, SelectedItemActivity::class.java).apply {
                putExtra("searchBusiness", popupEditText.text.toString())
            }
            popupEditText.text.clear()
            popupWindow.dismiss()

            imm.hideSoftInputFromWindow(token, 0)
            startActivity(
                intent,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )

        }
    }

    private val onPopupButtonClick = View.OnClickListener { view ->
        performSearch(view.windowToken)
    }

    override fun onItemClick(view: View, position: Int) {

        val intent = Intent(this, SelectedItemActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, viewAdapter.getItem(position)?.id); putExtra(
            "label",
            viewAdapter.getItem(position)?.name
        )
        }

        startActivity(
            intent,
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    private fun getCategories(onCreated: Boolean = true) {
        val db = FirebaseFirestore.getInstance()

        val progressBar = findViewById<ProgressBar>(R.id.progressbar)
        progressBar.visibility = View.VISIBLE // To show the ProgressBar

        db.collection("categories").orderBy("order").get().addOnSuccessListener { result ->
            for (document in result) {
                categoryList.add(
                    CategoryModel(
                        document.id,
                        document.data["name"].toString(),
                        document.data["order"].toString().toInt()
                    )
                )
            }
            progressBar.visibility = View.GONE
            if (categoryList.count() == 0) {
                showNoConnectionMessage()
            } else {
                updateAdapter()
            }
        }
    }

    private fun updateAdapter() {
        viewAdapter.categoryFilterList.addAll(categoryList)
        viewAdapter.notifyDataSetChanged()
    }
}