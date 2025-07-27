package com.sudh.weatherforecast

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.util.*

class SearchFragment : Fragment() {

    private lateinit var searchText: AutoCompleteTextView
    private lateinit var recentContainer: LinearLayout
    private var recentList = ArrayList<String>()
    private var latestCityList = listOf<String>()
    private var searchJob: Job? = null
    private var userSelectedFromDropdown = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchText = view.findViewById(R.id.searchText)
        recentContainer = view.findViewById(R.id.recentSearchContainer)

        searchText.threshold = 1

        searchText.setOnItemClickListener { _, _, _, _ ->
            userSelectedFromDropdown = true
            searchText.dismissDropDown()
        }

        searchText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length < 2) return

                userSelectedFromDropdown = false
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300) // debounce
                    try {
                        val api = RetrofitClient.geoApi
                        val results = api.getCoordinatesByCity(query, 5, BuildConfig.OPENWEATHER_API_KEY)

                        latestCityList = results.map {
                            if (it.country.isNotEmpty()) "${it.name}, ${it.country}" else it.name
                        }

                        withContext(Dispatchers.Main) {
                            if (!userSelectedFromDropdown) {
                                val adapter = ArrayAdapter(
                                    requireContext(),
                                    android.R.layout.simple_dropdown_item_1line,
                                    latestCityList
                                )
                                searchText.setAdapter(adapter)
                                searchText.showDropDown()
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("SearchFragment", "City fetch failed: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error fetching suggestions", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        searchText.setOnEditorActionListener { _, actionId, event ->
            val isDone = actionId == EditorInfo.IME_ACTION_DONE
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (isDone || isEnter) {
                handleCitySelection()
                true
            } else false
        }

        searchText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = searchText.compoundDrawables[2]
                if (drawableEnd != null) {
                    val width = drawableEnd.bounds.width()
                    val rightEdge = searchText.right
                    val padding = searchText.paddingEnd
                    val clickX = event.rawX
                    if (clickX >= (rightEdge - width - padding)) {
                        searchText.setText("")
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        updateRecentUI(recentContainer, recentList, searchText)

        view.findViewById<ImageView>(R.id.backButton).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun handleCitySelection() {
        val input = searchText.text.toString().trim()

        if (input.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a city name", Toast.LENGTH_SHORT).show()
            return
        }

        if (!latestCityList.contains(input)) {
            Toast.makeText(requireContext(), "Please select a valid city from dropdown", Toast.LENGTH_SHORT).show()
            return
        }

        val cleanCity = input.replaceFirstChar { it.uppercaseChar() }

        if (recentList.contains(cleanCity)) {
            recentList.remove(cleanCity)
        }
        recentList.add(0, cleanCity)
        if (recentList.size > 5) recentList.removeAt(recentList.size - 1)

        hideKeyboard(searchText)
        searchText.setText("")
        updateRecentUI(recentContainer, recentList, searchText)

        requireContext().getSharedPreferences("weather_prefs", Activity.MODE_PRIVATE).edit()
            .putString("city_name", cleanCity)
            .commit()

        startHome(cleanCity)
    }

    private fun startHome(cityName: String) {
        val activity = requireActivity() as WeatherActivity
        activity.switchToPage(0) // assuming HomeFragment is at index 0
    }



    private fun updateRecentUI(container: LinearLayout, recentList: ArrayList<String>, searchText: EditText) {
        container.removeAllViews()

        for (item in recentList) {
            val tag = TextView(requireContext())
            tag.text = item
            tag.setTextColor(Color.WHITE)
            tag.setPadding(30, 20, 30, 20)
            tag.textSize = 16f
            tag.setBackgroundColor(Color.parseColor("#33FFFFFF"))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(10, 10, 10, 10)
            tag.layoutParams = params

            tag.setOnClickListener {
                searchText.setText(item)
                searchText.setSelection(item.length)
            }

            container.addView(tag)
        }
    }

    private fun hideKeyboard(editText: EditText) {
        val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}
