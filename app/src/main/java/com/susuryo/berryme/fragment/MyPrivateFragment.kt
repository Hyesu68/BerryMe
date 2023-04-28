package com.susuryo.berryme.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.susuryo.berryme.MyAdapter
import com.susuryo.berryme.R

class MyPrivateFragment: Fragment() {
    private lateinit var adapter: MyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_public_private, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.gridView)
        recyclerView.layoutManager = GridLayoutManager(inflater.context, 3)
        adapter = MyAdapter(requireActivity(), true)
        recyclerView.adapter = adapter
        return view
    }

}