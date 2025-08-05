package com.ucb.pawapp.citizen.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ucb.pawapp.R

class ArticlesFragment : Fragment(R.layout.fragment_articles) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tv_articles).text = "Articles for animal welfare 📚"
    }
}
