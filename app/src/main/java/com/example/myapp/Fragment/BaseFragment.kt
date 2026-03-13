package com.example.myapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp.BaseActivity
import com.example.myapp.R

abstract class BaseFragment : Fragment() {
    protected open val TAG: String = this::class.java.simpleName

    protected lateinit var mRootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mRootView = inflater.inflate(getLayoutId(), container, false)
        Log.d(TAG, "${TAG} layout loaded: ${getLayoutId()}")
        initView()
        initListener()
        return mRootView
    }

    protected abstract fun getLayoutId(): Int

    protected open fun initView() {}

    protected open fun initListener() {}

    protected fun showToast(msg: String) {
        activity?.let { context->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    protected fun switchFragment(targetFragment: Fragment, addToBackStack: Boolean = true) {
        (activity as BaseActivity).switchFragment(R.id.activity_main, targetFragment,addToBackStack)
    }

    protected fun <T : View> findView(id: Int): T {
        return mRootView.findViewById(id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}