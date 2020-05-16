package com.example.gpsgame

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile.view.*


class ProfileFragment : Fragment() {

    var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private lateinit var viewOfLayout: View
    lateinit var activity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = context as MainActivity
        auth = FirebaseAuth.getInstance()

        var docRef = db.collection("users")
                                        .document(auth.currentUser?.uid.toString())
                                        .collection("placeItems")

        docRef.whereEqualTo( "completed", true ).get().addOnSuccessListener { result ->

            Log.d("profilefrag", result.documents.size.toString())

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewOfLayout = inflater.inflate(R.layout.fragment_profile, container, false)
        viewOfLayout.user_full_name.text = activity.user_full_name

        return viewOfLayout
    }

}
