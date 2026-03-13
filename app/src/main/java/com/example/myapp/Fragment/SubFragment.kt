package com.example.myapp.Fragment

import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.fragment.BaseFragment
import kotlinx.coroutines.launch
import com.example.myapp.DB.UserManage
import com.example.myapp.Data.RecordModel
import com.example.myapp.Data.RecordAdapter
import java.util.Locale
import com.example.myapp.Data.RecordViewModel

class MainFragment : BaseFragment() {
    override val TAG: String = "MainFragment"

    override fun getLayoutId(): Int = R.layout.fragment_main

    override fun initListener() {
        val btnUp: Button = findView(R.id.btn_account_up)
        val btnNew: Button = findView(R.id.btn_account_new)

        btnUp.setOnClickListener {
            switchFragment(UpFragment())
        }
        btnNew.setOnClickListener {
            switchFragment(NewFragment())
        }
    }
}

class NewFragment : BaseFragment() {
    override val TAG: String = "NewFragment"

    private lateinit var etUsername: EditText
    private lateinit var etUserPwd: EditText
    private lateinit var etReUserPwd: EditText

    override fun getLayoutId(): Int = R.layout.fragment_new

    override fun initView() {

        etUsername = findView(R.id.et_username)
        etUserPwd = findView(R.id.et_userpwd)
        etReUserPwd = findView(R.id.et_reuserpwd)
    }

    override fun initListener() {
        val btnNew: Button = findView(R.id.btn_account_new)
        btnNew.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val userPwd = etUserPwd.text.toString().trim()
            val reUserPwd = etReUserPwd.text.toString().trim()

            when {
                username.isEmpty() -> showToast(getString(R.string.username_null))
                userPwd.isEmpty() -> showToast(getString(R.string.pwd_null))
                reUserPwd != userPwd -> showToast(getString(R.string.pwd_error))
                else -> {
                    lifecycleScope.launch {
                        val isSuccess = UserManage.newUser(requireContext(), username, userPwd)
                        if (isSuccess) {
                            showToast(getString(R.string.new_success))
                            switchFragment(MainFragment())
                        } else {
                            showToast(getString(R.string.user_repeat))
                        }
                    }
                }
            }
        }
    }
}

class UpFragment : BaseFragment() {
    override val TAG: String = "UpFragment"

    private lateinit var etUsername: EditText
    private lateinit var etUserPwd: EditText

    override fun getLayoutId(): Int = R.layout.fragment_up

    override fun initView() {
        etUsername = findView(R.id.et_username)
        etUserPwd = findView(R.id.et_userpwd)
    }

    override fun initListener() {
        val btnUp: Button = findView(R.id.btn_account_up)
        btnUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val userPwd = etUserPwd.text.toString().trim()

            // 输入校验
            when {
                username.isEmpty() -> showToast(getString(R.string.username_null))
                userPwd.isEmpty() -> showToast(getString(R.string.pwd_null))
                else -> {
                    lifecycleScope.launch {
                        val isSuccess = UserManage.loginUser(requireContext(), username, userPwd)
                        if (isSuccess) {
                            showToast(getString(R.string.up_success))
                            switchFragment(ActionFragment())
                        } else {
                            showToast(getString(R.string.user_error))
                        }
                    }
                }
            }
        }
    }
}
class ActionFragment : BaseFragment() {
    override val TAG: String = "ActionFragment"

    override fun getLayoutId(): Int = R.layout.fragment_action

    private lateinit var rvRecordList: RecyclerView
    private val viewModel by viewModels<RecordViewModel>(
        ownerProducer = { requireActivity() } // 关键：整个Activity共用一个ViewModel
    )
    private lateinit var recordAdapter: RecordAdapter

    private fun addRecordList(data: RecordModel) {
        viewModel.recordList.add(data)
        recordAdapter.notifyItemInserted(viewModel.recordList.size - 1)
    }

    override fun initView() {
        rvRecordList = findView(R.id.card_list)
        rvRecordList.layoutManager = LinearLayoutManager(context)
        recordAdapter = RecordAdapter(viewModel.recordList)
        rvRecordList.adapter = recordAdapter
        recordAdapter.onItemDeleteListener = { position ->
            recordAdapter.removeItem(position)
        }

        arguments?.let { bundle ->
            val recordModel = bundle.getSerializable("KEY_RECORD_MODEL") as? RecordModel
            recordModel?.let {
                addRecordList(it)
                bundle.remove("KEY_RECORD_MODEL")
            }
        }
    }

    override fun initListener(){
        val btnGoOn: Button=findView(R.id.btn_goOn)
        btnGoOn.setOnClickListener {
            switchFragment(RecordFragment())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("RECORD_LIST", ArrayList(viewModel.recordList))
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            @Suppress("UNCHECKED_CAST")
            val savedList = it.getSerializable("RECORD_LIST") as? ArrayList<RecordModel>
            savedList?.let { list ->
                viewModel.recordList.clear()
                viewModel.recordList.addAll(list)
                recordAdapter.notifyDataSetChanged()
            }
        }
    }
}

class RecordFragment:BaseFragment(){
    override val TAG:String="RecordFragment"

    override fun getLayoutId(): Int=R.layout.fragment_record

    private lateinit var tvTime: TextView
    private var totalTime = 0
    private var isTiming = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun startTimer() {
        if (!isTiming) {
            isTiming = true
            mainHandler.post(timeRun)
        }
    }

    private fun stopTimer() {
        if (isTiming) {
            isTiming = false
            mainHandler.removeCallbacks(timeRun)
        }
    }
    private fun formatTime(time:Int):String{
        val minutes=time/60
        val secs=time%60
        // Locale.getDefault()：适配手机系统语言，避免格式错乱
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }
    private fun updateTimeDisplay() {
        tvTime.text = formatTime(totalTime)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
        mainHandler.removeCallbacksAndMessages(null)
    }

    private val timeRun = object : Runnable {
        override fun run() {
            if (isTiming) {
                totalTime++
                updateTimeDisplay()
                mainHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun initView() {
        tvTime = findView(R.id.tv_time)
        tvTime.text = "00:00"
        startTimer()
    }

    override fun initListener(){
        val btnYes: ImageButton =findView(R.id.btn_record)
        btnYes.setOnClickListener {
            stopTimer()
            val finalTime = formatTime(totalTime)
            val bundle = Bundle()
            bundle.putString("KEY_RECORD_TIME", finalTime)
            val settingFragment = SettingRecordFragment().apply {
                arguments = bundle
            }
            switchFragment(settingFragment)
        }
    }
}

class SettingRecordFragment:BaseFragment(){
    override val TAG:String="SettingRecordFragment"

    override fun getLayoutId(): Int=R.layout.fragment_setting_record

    private lateinit var tvTime: TextView

    private lateinit var etRecordName: EditText

    private var finalTime: String = "00:00"

    override fun initView() {
        tvTime = findView(R.id.tv_time)
        etRecordName = findView(R.id.et_recordName)

        arguments?.let { bundle ->
            val time = bundle.getString("KEY_RECORD_TIME")
            if (!time.isNullOrEmpty()) {
                finalTime=time
                tvTime.text="$finalTime"
            }
        }
    }

    override fun initListener(){
        val btnYes: Button=findView(R.id.btn_account_new)
        btnYes.setOnClickListener {
            val recordTitle = etRecordName.text.toString().trim()

            if (recordTitle.isEmpty()) {
                showToast(getString(R.string.record_title))
                return@setOnClickListener
            }
            val recordModel = RecordModel(
                time = finalTime,
                title = recordTitle
            )

            val actionFragment = ActionFragment().apply {
                val bundle = Bundle()
                bundle.putSerializable("KEY_RECORD_MODEL", recordModel)
                arguments = bundle
            }
            switchFragment(actionFragment, addToBackStack = true)
        }
    }
}