package com.example.gpsgame

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*


class ProfileFragment : Fragment() {

    var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private lateinit var viewOfLayout: View
    lateinit var activity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = context as MainActivity
        auth = FirebaseAuth.getInstance()



    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val docRef = db.collection("users")
            .document(auth.currentUser?.uid.toString())
            .collection("placeItems")

//        Fade in text view?
        docRef.whereEqualTo( "completed", true ).get().addOnSuccessListener { result ->

            viewOfLayout.stars_value.text = result.documents.size.toString()

//            TODO Add medal calc value
            viewOfLayout.medal_value.text = "3"
        }


        val date = Date(Date().time - 86400000 * 7)

        Log.d("profilef", date.toString())

        docRef.whereGreaterThanOrEqualTo("created", date).get().addOnSuccessListener { result ->

            Log.d("profilef", result.documents.size.toString())

            if ( result.documents.size > 0 ) {

                val total = result.documents.size.toFloat()

                var completed = 0.0F

                for (document in result.documents) {

                    if (document["completed"] as Boolean) completed++
                }

                var percentComplete = (completed / total * 100).toInt()

                viewOfLayout.progress_value_text.text = percentComplete.toString() + "%"

                if ( percentComplete == 0) percentComplete = 1
                viewOfLayout.circle_progress_bar.setProgress(percentComplete, true)

                Log.d("profilef", "${total} ${completed}")
                Log.d("profilef", "${completed / total * 100}")
            }
            else {
                viewOfLayout.progress_value_text.text = "0%"
                viewOfLayout.circle_progress_bar.progress = 1
            }
        }


        viewOfLayout = inflater.inflate(R.layout.fragment_profile, container, false)
        viewOfLayout.user_full_name.text = activity.user_full_name

        return viewOfLayout
    }

}
