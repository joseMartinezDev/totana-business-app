package com.josetotana.totanabusiness.categoryItems

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.transition.Fade
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.josetotana.totanabusiness.R
import com.josetotana.totanabusiness.itemCard.ItemCardActivity
import com.josetotana.totanabusiness.utils.TextFilter
import kotlinx.android.synthetic.main.activity_selected_item.*

class SelectedItemActivity : AppCompatActivity(),
    CategoryItemsRecyclerViewAdapter.CategoryItemClickListener {

    private val itemList = mutableListOf<CategoryItemModel>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: CategoryItemsRecyclerViewAdapter
    private lateinit var sharedPref: SharedPreferences
    private lateinit var categoryId: String
    private var isFavourites: Boolean = false
    private lateinit var searchText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)

            // set an exit transition
            enterTransition = Fade()
            exitTransition = Fade()
        }

        setContentView(R.layout.activity_selected_item)

        recyclerView = findViewById(R.id.rvCategoryItems)

        recyclerView.layoutManager = LinearLayoutManager(this)
        viewAdapter = CategoryItemsRecyclerViewAdapter(itemList)
        viewAdapter.setClickListener(this)
        recyclerView.adapter = viewAdapter
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        categoryId = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
        isFavourites = intent.getBooleanExtra("favourites", false)
        searchText = intent.getStringExtra("searchBusiness") ?: ""
        val categoriesSelectedLabel =
            if (searchText.isEmpty()) intent.getStringExtra("label") else "Buscar: $searchText"
        categoriesSelectedLabelTv.text = categoriesSelectedLabel
//        supportActionBar?.title = HtmlCompat.fromHtml(
//            "<font color=\"#FFFFFF\">" + getString(R.string.app_name) + "</font>",
//            HtmlCompat.FROM_HTML_MODE_LEGACY
//        )
        loadItems()

        pullToRefreshItems.setOnRefreshListener {
            itemList.clear()
            viewAdapter.itemsFilterList.clear()
            loadItems(false)
            pullToRefreshItems.isRefreshing = false
        }
    }

    private fun showNoConnectionMessage() {
        Toast.makeText(this, R.string.noConectionMessageItems, Toast.LENGTH_LONG).show()

    }

    private fun loadItems(onCreated: Boolean = true) {
        when {
            isFavourites -> {
                sharedPref = getSharedPreferences(
                    resources.getString(R.string.fav_storage_file_name),
                    Context.MODE_PRIVATE
                )
                if (sharedPref.all.count() > 0) {
                    getAllItems("", onCreated)
                } else {
                    //there are no favourites
                    showElements(getString(R.string.noFavouritesFound))
                }

            }
            searchText.isNotEmpty() -> {
                getAllItems(searchText, onCreated)
            }
            categoryId != null -> {
                getItems(categoryId, onCreated)
            }
        }

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

    private fun getAllItems(businessName: String, onCreated: Boolean = true) {

        val db = FirebaseFirestore.getInstance()

        //in order to perform a smart search we need to get the whole business list
        val businessList = mutableListOf<CategoryItemModel>()

        db.collection("categoryItems").get().addOnSuccessListener { result ->
            for (document in result) {

                businessList.add(
                    CategoryItemModel(
                        document.id,
                        document.data["name"].toString(),
                        document.data["imageURL"].toString(),
                        document.data["address"].toString(),
                        document.data["phone"].toString(),
                        document.data["whatsapp"].toString(),
                        document.data["maps"].toString()
                    )
                )

            }
            if (businessList.count() > 0) {

                if (onCreated) {
                    if (businessName.isEmpty()) {
                        //favourites
                        getFavourites(businessList)
                    } else {
                        //searching
                        searchBusiness(businessList, businessName)
                    }
                } else {
                    viewAdapter.itemsFilterList.addAll(businessList)
                    viewAdapter.notifyDataSetChanged()
                }
            } else showNoConnectionMessage()

        }
    }

    private fun searchBusiness(
        businessList: MutableList<CategoryItemModel>,
        businessName: String,
        onCreated: Boolean = true
    ) {

        val nameNorm = TextFilter.getNormalizeText(businessName)
        itemList.addAll(businessList.filter { element ->
            TextFilter.getNormalizeText(element.name).contains(nameNorm)
        })
        showElements(getString(R.string.noBusinessFoundText), onCreated)

    }

    private fun showElements(errorMessage: String, onCreated: Boolean = true) {
        if (itemList.isEmpty()) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        } else {
            updateAdapter()
        }
    }

    private fun updateAdapter() {
        viewAdapter.itemsFilterList.addAll(itemList)
        viewAdapter.notifyDataSetChanged()
        if (itemList.count() == 1) {
            startItemCardActivity(0)
        }
    }

    private fun getFavourites(
        businessList: MutableList<CategoryItemModel>,
        onCreated: Boolean = true
    ) {

        itemList.addAll(businessList.filter { element -> sharedPref.contains(element.id) })
        showElements(getString(R.string.noFavouritesFound))
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

    private fun getItems(categoryId: String, onCreated: Boolean = true) {
        val db = FirebaseFirestore.getInstance()

        db.collection("categoryItems").orderBy("order").whereEqualTo("catId", categoryId).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    itemList.add(
                        CategoryItemModel(
                            document.id,
                            document.data["name"].toString(),
                            document.data["imageURL"].toString(),
                            document.data["address"].toString(),
                            document.data["phone"].toString(),
                            document.data["whatsapp"].toString(),
                            document.data["maps"].toString()
                        )
                    )
                }

                if (itemList.count() > 0) updateAdapter() else showNoConnectionMessage()
            }
    }

    override fun onCategoryItemClick(view: View, position: Int) {
        startItemCardActivity(position)
    }

    private fun startItemCardActivity(position: Int) {
        val intent = Intent(this, ItemCardActivity::class.java).apply {
            putExtra("CategoryModel", viewAdapter.getItem(position))
        }
        startActivity(
            intent,
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    }
}