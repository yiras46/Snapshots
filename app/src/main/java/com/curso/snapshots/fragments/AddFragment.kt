package com.curso.snapshots.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.curso.snapshots.R
import com.curso.snapshots.databinding.FragmentAddBinding
import com.curso.snapshots.entities.Snapshot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class AddFragment : Fragment() {

    private val PATH_SNAPSHOT = "snapshots"

    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStorageReference:StorageReference
    private lateinit var mDataBaseReference:DatabaseReference

    private var mPhotoSelectedUri:Uri? = null

    private val galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode  == Activity.RESULT_OK){
            mPhotoSelectedUri = it.data?.data
            mBinding.imageViewPhoto.setImageURI(mPhotoSelectedUri)
            mBinding.textInputLayoutTitle.visibility = View.VISIBLE
            mBinding.textViewMessage.text = getString(R.string.escribir_titulo)
            mBinding.btnSelect.visibility = View.INVISIBLE
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnSelect.setOnClickListener { openGallery() }
        mBinding.btnPost.setOnClickListener { postSnapshot() }

        mStorageReference = FirebaseStorage.getInstance().reference
        mDataBaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
    }

    private fun openGallery() {

        val intent  = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(intent)
    }

    private fun postSnapshot() {

        mBinding.progressBarUpload.progress = 0
        mBinding.progressBarUpload.visibility = View.VISIBLE
        mBinding.textViewMessage.visibility = View.INVISIBLE

        val key = mDataBaseReference.push().key!!

        mStorageReference
            .child(PATH_SNAPSHOT)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(key)
            .putFile(mPhotoSelectedUri!!)
            .addOnProgressListener {
                mBinding.progressBarUpload.progress = (it.bytesTransferred / it.totalByteCount).toInt()*100
            }
            .addOnCompleteListener {
                mBinding.progressBarUpload.visibility = View.INVISIBLE
                mBinding.btnSelect.visibility = View.VISIBLE
                mBinding.textViewMessage.visibility = View.VISIBLE
                mBinding.imageViewPhoto.setImageURI(null)
                mBinding.textInputLayoutTitle.visibility = View.GONE
                mBinding.textViewMessage.text = getString(R.string.seleccione_una_foto)
            }
            .addOnSuccessListener { itSuccess ->
                Toast.makeText(requireContext(), getString(R.string.foto_subida_correctamente), Toast.LENGTH_SHORT).show()
                itSuccess.storage.downloadUrl.addOnSuccessListener {
                    saveSnapshot(key,it.toString(),mBinding.editTextTitle.text.toString().trim())
                    mBinding.editTextTitle.text!!.clear()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveSnapshot(key:String, url:String, title: String){

        val snapshot = Snapshot(title = title, photoUrl = url)
        mDataBaseReference.child(key).setValue(snapshot)
    }
}