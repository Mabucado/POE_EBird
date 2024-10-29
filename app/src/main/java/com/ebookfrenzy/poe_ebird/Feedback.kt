package com.ebookfrenzy.poe_ebird

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Feedback.newInstance] factory method to
 * create an instance of this fragment.
 */
class Feedbacks : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var  often: TextView
    lateinit var  motive:TextView
    lateinit var  improve:TextView
    lateinit var  message:TextView
    lateinit var  submit: Button

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
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        often=view.findViewById(R.id.edtOften)
        motive=view.findViewById(R.id.edtMotive)
        improve=view.findViewById(R.id.edtImprove)
        message=view.findViewById(R.id.edtMessage)
        submit=view.findViewById(R.id.btnSubmit)
       var model= globalModel

        submit.setOnClickListener {
            var of=often.text.toString()
            var mot=motive.text.toString()
            var imp=improve.text.toString()
            var mes=message.text.toString()
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(calendar.time)
            model.feedbackList.add(
                Feedback(1,mes,of,mot,imp,currentDate,model.loggedInUser!! )
            )
            Toast.makeText(requireContext(), "Feedback submitted", Toast.LENGTH_SHORT).show()
            saveModelDataToFirestore(model)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Feedback.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Feedbacks().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}