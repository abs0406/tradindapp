package com.example.projetmobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.projetmobile.fragments.AddArticleFragment
import com.example.projetmobile.fragments.ChatListFragment
import com.example.projetmobile.fragments.HomeFragment
import com.example.projetmobile.fragments.ProfileFragment
import com.example.projetmobile.fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null

            when (item.itemId) {
                R.id.navigation_home -> selectedFragment = HomeFragment()
                R.id.navigation_search -> selectedFragment = SearchFragment()
                R.id.navigation_add -> selectedFragment = AddArticleFragment()
                R.id.navigation_chat -> selectedFragment = ChatListFragment()
                R.id.navigation_profile -> selectedFragment = ProfileFragment()
            }

            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
            }

            true
        }

        // Afficher le fragment Home par défaut
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }
}