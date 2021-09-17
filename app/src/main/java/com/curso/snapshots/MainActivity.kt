package com.curso.snapshots

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.curso.snapshots.databinding.ActivityMainBinding
import com.curso.snapshots.fragments.AddFragment
import com.curso.snapshots.fragments.HomeFragment
import com.curso.snapshots.fragments.ProfileFragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 18

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener

    private var mFirebaseAuth:FirebaseAuth? = null

    private val loginResult  = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK) {
            Toast.makeText(this, "Log realizado", Toast.LENGTH_SHORT).show()
        }else{
            if(IdpResponse.fromResultIntent(it.data) == null){ //El usuario canceló el login
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        setupAuth()
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener { mAuthListener }
        checkUserLoggin()
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener (mAuthListener)
    }

    private fun setupAuth() {

        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { checkUserLoggin() }
    }

    fun checkUserLoggin(){
        if(FirebaseAuth.getInstance().currentUser == null){
            val intentLogin = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()))
                .build()
            loginResult.launch(intentLogin)
        }
    }

    private fun setupBottomNav(){

        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        mFragmentManager = supportFragmentManager

        //Agregamos Profile y lo ocultamos
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, profileFragment, ProfileFragment::class.java.name)
            .hide(profileFragment).commit()

        //Agregamos Add y lo ocultamos
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, addFragment, AddFragment::class.java.name)
            .hide(addFragment).commit()

        //Agregamos home y la dejamos visible como fragmento activo
        mActiveFragment = homeFragment
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, homeFragment, HomeFragment::class.java.name)
            .commit()

        //Selección en la bottomNavigation Bar
        mainBinding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.action_home -> {
                    showFragment(homeFragment)
                    true
                }
                R.id.action_add -> {
                    showFragment(addFragment)
                    true
                }
                R.id.action_profile -> {
                    showFragment(profileFragment)
                true
                }
                else-> false
            }
        }

        //Reselección
        mainBinding.bottomNav.setOnItemReselectedListener {
            when(it.itemId){
                R.id.action_home -> {
                    homeFragment.goToTop()
                }
            }
        }
    }

    private fun showFragment(fragment:Fragment){

        if(fragment != mActiveFragment){
            mFragmentManager.beginTransaction().hide(mActiveFragment).show(fragment).commit()
            mActiveFragment = fragment
        }
    }
}