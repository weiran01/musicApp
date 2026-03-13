package com.example.myapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapp.Fragment.ActionFragment
import com.example.myapp.Fragment.MainFragment
import com.example.myapp.Fragment.NewFragment
import com.example.myapp.Fragment.RecordFragment
import com.example.myapp.Fragment.SettingRecordFragment
import com.example.myapp.Fragment.UpFragment

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun switchFragment(
        containerId: Int,
        targetFragment: Fragment,
        addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(containerId, targetFragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.activity_main)
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
            return
        }


        when (currentFragment) {
            is UpFragment, is NewFragment -> {
                switchFragment(R.id.activity_main, MainFragment(), addToBackStack = false)
            }
            is ActionFragment, is MainFragment -> {
                finish()
            }
            is SettingRecordFragment,is RecordFragment ->{
                // 直接弹出回退栈，回到跳转前的ActionFragment（无需新建）
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStackImmediate()
                } else {
                    // 回退栈为空时，才复用/新建ActionFragment
                    val existingActionFragment = supportFragmentManager.fragments.find { it is ActionFragment } as? ActionFragment
                    switchFragment(R.id.activity_main, existingActionFragment ?: ActionFragment(), addToBackStack = false)
                }
            }
            else -> super.onBackPressed()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            switchFragment(R.id.activity_main, MainFragment())
        }
    }
}