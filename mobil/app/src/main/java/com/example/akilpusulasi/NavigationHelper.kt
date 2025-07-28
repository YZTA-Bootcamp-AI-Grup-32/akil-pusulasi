package com.example.akilpusulasi

import android.app.Activity
import android.content.Intent
import android.widget.LinearLayout

fun setupBottomNavigation(activity: Activity, current: String) {
    val btnHome = activity.findViewById<LinearLayout>(R.id.btnHome)
    val btnGame = activity.findViewById<LinearLayout>(R.id.btnGame)
    val btnJournal = activity.findViewById<LinearLayout>(R.id.btnJournal)

    btnHome.isSelected = current == "home"
    btnGame.isSelected = current == "game"
    btnJournal.isSelected = current == "journal"

    btnHome.setOnClickListener {
        if (current != "home") {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }
    }

    btnGame.setOnClickListener {
        if (current != "game") {
            activity.startActivity(Intent(activity, GameActivity::class.java))
            activity.finish()
        }
    }

    btnJournal.setOnClickListener {
        if (current != "journal") {
            activity.startActivity(Intent(activity, JournalActivity::class.java))
            activity.finish()
        }
    }
}
