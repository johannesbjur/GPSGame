package com.example.gpsgame

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.*


class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private lateinit var viewOfLayout: View
    private lateinit var activity: MainActivity

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

        viewOfLayout = inflater.inflate(R.layout.fragment_profile, container, false)
        viewOfLayout.user_full_name.text = activity.userFullName
        viewOfLayout.settingsBtn?.setOnClickListener {

            activity.goToSettings()
        }

//      Get user name from db and set name text to user name or Guest
        db.collection( "users" )
            .document( auth.currentUser?.uid.toString() ).addSnapshotListener { querySnapshot, e ->

                if (querySnapshot != null) {
                    user_full_name.text = ( querySnapshot.data?.get("name") ?: "Guest" ) as String
                }
            }

        val docRef = db.collection("users")
            .document(auth.currentUser?.uid.toString())
            .collection("placeItems")

//        Fade in text view?
        docRef.whereEqualTo( "completed", true ).addSnapshotListener { querySnapshot, e ->

            if ( querySnapshot != null && querySnapshot.documents.size > 0 ) {
//            TODO Add more "ranks"
                when (querySnapshot.documents.size) {
                    in 0..10 -> viewOfLayout.user_rank_text.text = "Beginner"
                    in 10..20 -> viewOfLayout.user_rank_text.text = "Novice"
                }

                viewOfLayout.stars_value.text = querySnapshot.documents.size.toString()

//            TODO Add medal calc value
                viewOfLayout.medal_value.text = "3"
            }
        }

//        val date = Date(Date().time - 86400000 * 7)
        val weekAgoDate = DateTime.now().minusDays(7).withHourOfDay(0).withMinuteOfHour(0)


        docRef.whereGreaterThanOrEqualTo("created", weekAgoDate.toDate()).addSnapshotListener { querySnapshot, e ->


            if ( querySnapshot != null && querySnapshot.documents.size > 0 ) {

                val total = querySnapshot.documents.size.toFloat()

                var completed = 0.0F

                val map = mutableMapOf<String, Int>()

//                Create date set map
                for (document in querySnapshot.documents) {

                    if ( document["completed"] as Boolean ) completed++

//                    TODO check if completed
                    val itemDate = DateTime((document["created"] as Timestamp).toDate())
                    val day = itemDate.dayOfMonth().get()

                    map[itemDate.toString()] =
                        if ( map[itemDate.toString()] != null ) map[itemDate.toString()]!! + 1
                        else 1
                }

                Log.d("profilef", map.toString())


                val series = LineGraphSeries<DataPoint>()

                for ( (index, value) in map ) {
                    val x = DateTime(index).toDate()

                    Log.d("profilef", x.toString())
                    val dp = DataPoint(x, value.toDouble())
                    series.appendData( dp, true, 5)
                }

                series.color = Color.parseColor("#FF9900")
                series.isDrawDataPoints = true
                series.thickness = 14

                viewOfLayout.line_graph.addSeries(series)
                viewOfLayout.line_graph.gridLabelRenderer.gridColor = 80000000

                viewOfLayout.line_graph.viewport.setMinY(0.0)
                viewOfLayout.line_graph.viewport.setMaxY(2.0)
                viewOfLayout.line_graph.viewport.isYAxisBoundsManual = true

//                viewOfLayout.line_graph.viewport.setMinX(weekAgoDate.dayOfMonth().get().toDouble())
//                viewOfLayout.line_graph.viewport.setMaxX(weekAgoDate.dayOfMonth().get().toDouble() + 7)
                viewOfLayout.line_graph.viewport.isXAxisBoundsManual = true



                var percentComplete = (completed / total * 100).toInt()

                viewOfLayout.progress_value_text.text = percentComplete.toString() + "%"

                if ( percentComplete == 0 ) percentComplete = 1
                viewOfLayout.circle_progress_bar.setProgress(percentComplete, true)

                Log.d("profilef", "${total} ${completed}")
                Log.d("profilef", "${completed / total * 100}")
            }
            else {
                viewOfLayout.progress_value_text.text = "0%"
                viewOfLayout.circle_progress_bar.progress = 1
            }
        }

        return viewOfLayout
    }

}
