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
import java.util.*
import kotlin.collections.HashMap

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

    @ExperimentalStdlibApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val data: MutableMap<String, Any> = HashMap()

        viewOfLayout = inflater.inflate(R.layout.fragment_settings, container, false)
        viewOfLayout.settingsBackBtn?.setOnClickListener {

            data["name"] = viewOfLayout.usernameInput.text.toString().capitalize(Locale.ROOT)
            data["email"] = viewOfLayout.emailInput.text.toString()
            data["phone"] = viewOfLayout.emailInput.text.toString()
            db.collection( "users" )
                .document( auth.currentUser?.uid.toString() )
                .update( data )

            activity.goToProfile()
        }

        val docRef = db.collection("users")
            .document(auth.currentUser?.uid.toString())

        docRef.get().addOnSuccessListener { result ->

            if ( result.get("name") != null && result.get("name") != "" )   viewOfLayout.usernameInput.hint = result.get("name").toString()
            if ( result.get("email") != null && result.get("email") != "" ) viewOfLayout.emailInput.hint = result.get("email").toString()
            if ( result.get("phone") != null && result.get("phone") != "" ) viewOfLayout.phoneInput.hint = result.get("phone").toString()
        }

        return viewOfLayout
    }


}
