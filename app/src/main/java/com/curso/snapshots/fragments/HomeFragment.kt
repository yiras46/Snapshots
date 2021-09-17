package com.curso.snapshots.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.curso.snapshots.HomeAux
import com.curso.snapshots.R
import com.curso.snapshots.databinding.FragmentHomeBinding
import com.curso.snapshots.databinding.ItemSnapshopBinding
import com.curso.snapshots.entities.Snapshot
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase


class HomeFragment : Fragment(), HomeAux {

    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>
    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mLayoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = FirebaseDatabase.getInstance().reference.child("snapshots")


        //val options = FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query, Snapshot::class.java).build()

        val options = FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query) {
            val snapshot = it.getValue(Snapshot::class.java)
            snapshot!!.id = it.key!! //Asignamos al objeto recuperado la key, para meterla como id dentro de nuestro objeto.
                                    //Por otra parte, en el data class excluímos el id, ya que no se necesita almacenar
            snapshot
        }.build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>(options){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
                val viewSnapshotHolder = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_snapshop, parent, false)
                return SnapshotHolder(viewSnapshotHolder)
            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {

                val snapshot = getItem(position)

                with(holder){
                    setListener(snapshot)
                    binding.tvTitle.text = snapshot.title
                    binding.cbLike.text = snapshot.likeList.keys.size.toString()
                    FirebaseAuth.getInstance().currentUser?.let {
                        binding.cbLike.isChecked = snapshot.likeList.containsKey(it.uid)
                    }

                    Glide.with(requireContext())
                        .load(snapshot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .into(binding.imgPhoto)
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()
                notifyDataSetChanged() //FIX para crash cuando se pausa el fragmento
                mBinding.progressBar.visibility = View.GONE
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(requireContext(), error.details, Toast.LENGTH_SHORT).show()
            }
        }

        mLayoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAdapter.startListening()
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAdapter.stopListening()
    }

    override fun goToTop() {
        mBinding.recyclerView.smoothScrollToPosition(0)
    }

    private fun deleteSnapshot(snapshot: Snapshot){

        FirebaseDatabase.getInstance().reference
            .child("snapshots")
            .child(snapshot.id).removeValue()
    }

    private fun setLike(snapshot: Snapshot, like:Boolean){

        with(FirebaseDatabase.getInstance().reference.child("snapshots")
            .child(snapshot.id).child("likeList")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)){

            if(like){
                setValue(like)
            }else{
                removeValue()
            }
        }
    }

    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemSnapshopBinding.bind(view)

        fun setListener(snapshot: Snapshot){
            binding.btnDelete.setOnClickListener { deleteSnapshot(snapshot) }
            binding.cbLike.setOnCheckedChangeListener { _, isChecked ->  setLike(snapshot, isChecked)}
        }
    }
}