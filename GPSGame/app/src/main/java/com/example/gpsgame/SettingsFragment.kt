package com.example.gpsgame

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {

    private lateinit var viewOfLayout: View
    private lateinit var activity: MainActivity

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = context as MainActivity
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewOfLayout = inflater.inflate(R.layout.fragment_settings, container, false)
        viewOfLayout.settingsBackBtn?.setOnClickListener {

            activity.goToProfile()
        }

        var docRef = db.collection("users")
            .document(auth.currentUser?.uid.toString())

        docRef.get().addOnSuccessListener { result ->

            Log.d("!!!", result.get("name").toString())

            if ( result.get("name") != null ) {

                viewOfLayout.usernameInput.hint = result.get("name").toString()
            }
        }


        viewOfLayout.usernameInput.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                val data: MutableMap<String, Any> = HashMap()

                data["name"] = viewOfLayout.usernameInput.text.toString()
                db.collection( "users" )
                    .document( auth.currentUser?.uid.toString() )
                    .set( data )

                return@OnKeyListener true
            }
            false
        })





        return viewOfLayout
    }


}
