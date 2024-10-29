package com.ebookfrenzy.poe_ebird

import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Slider.newInstance] factory method to
 * create an instance of this fragment.
 */
class Slider : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var slideAdapter:SlideAdapter
    lateinit var viewPager:ViewPager2
    private var isMapInteracting = false
    private val TAG = "SliderFragment"
    private lateinit var gestureDetector: GestureDetector
    lateinit var map: Button
    lateinit var settings: ImageButton
    lateinit var sliderLayout: FrameLayout
    lateinit var frag:Map
    lateinit var fragManager: FragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_slider, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        slideAdapter=SlideAdapter(this)
        viewPager=view.findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter=slideAdapter
        map=view.findViewById(R.id.btnMap)
        settings=view.findViewById(R.id.btnsettings)
        sliderLayout=view.findViewById(R.id.sliderFrameLayout)
        val arr :ArrayList<String> = arrayListOf("Achievements","OBS","Feed")

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout,viewPager){
                tab, position -> tab.text=arr.get(position)
        }.attach()
        map.setOnClickListener {
            frag=Map()
            fragManager= requireFragmentManager()
            val fragTrans=fragManager.beginTransaction()
            fragTrans.replace(R.id.frameLayout,frag)
            fragTrans.commit()
        }
        settings.setOnClickListener {
            val settingsFragment = SettingsFragment()
            settingsFragment.show(requireFragmentManager(), "SettingsFragment")
        }


        // Gesture detector for handling swipes
        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                Log.d("GestureDetector", "Scroll detected.")
                // Return true to consume the scroll
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                Log.d("GestureDetector", "Fling detected.")
                // Return true to consume the fling
                return true
            }
        })

        // Set a touch listener on the ViewPager2 to capture all touch events
        viewPager.setOnTouchListener { _, event ->
            if (gestureDetector.onTouchEvent(event)) {
                // Gesture was detected
                return@setOnTouchListener true
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> Log.d("ViewPager2Touch", "Touch down event detected.")
                MotionEvent.ACTION_MOVE -> Log.d("ViewPager2Touch", "Touch move event detected.")
                MotionEvent.ACTION_UP -> Log.d("ViewPager2Touch", "Touch up event detected.")
            }
            false // Let ViewPager2 handle the event if not consumed
        }

    }


    private fun destroySpecificFragment(position: Int) {
        slideAdapter.removeFragment(position) // Call this method to remove the specific fragment
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Slider.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Slider().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}