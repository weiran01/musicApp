package com.example.myapp.Fragment

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.Action.AudioLogic
import com.example.myapp.Action.AudioPlayer
import com.example.myapp.DB.AudioManage
import com.example.myapp.DB.AudioRecord
import com.example.myapp.R
import com.example.myapp.fragment.BaseFragment
import kotlinx.coroutines.launch
import com.example.myapp.DB.UserManage
import com.example.myapp.Data.RecordModel
import com.example.myapp.Data.UserModel
import com.example.myapp.Data.RecordAdapter
import java.util.Locale
import com.example.myapp.Data.RecordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.remove

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

            when {
                username.isEmpty() -> showToast(getString(R.string.username_null))
                userPwd.isEmpty() -> showToast(getString(R.string.pwd_null))
                else -> {
                    lifecycleScope.launch {
                        val isSuccess = UserManage.loginUser(requireContext(), username, userPwd)
                        if (isSuccess) {
                            showToast(getString(R.string.up_success))
                            val db = AudioManage.getInstance(requireContext())
                            val loginUser = db.userDao().getUser(username)
                            val currentUserId = loginUser?.id?:0

                            val userModel = UserModel(
                                username=username,
                                userpwd=userPwd,
                                userId = currentUserId)
                            val actionFragment = ActionFragment().apply {
                                arguments = Bundle().apply {
                                    putSerializable("KEY_USER", userModel)
                                }
                            }
                            switchFragment(actionFragment, addToBackStack = true)
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

    private var userModel: UserModel? = null
    private var currentUserId: Int = 0
    private lateinit var rvRecordList: RecyclerView
    private val viewModel by viewModels<RecordViewModel>(
        ownerProducer = { requireActivity() } // 关键：整个Activity共用一个ViewModel
    )
    private lateinit var recordAdapter: RecordAdapter

    private suspend fun saveRecordToDatabase(recordModel: RecordModel) {
        if (recordModel.audioPath.isNullOrEmpty()) return

        val db = AudioManage.getInstance(requireContext())

        val audioRecord = AudioRecord(
            userId = currentUserId,
            title = recordModel.title,
            time = recordModel.time,
            audioPath = recordModel.audioPath
        )
        db.audioDao().insertAudio(audioRecord)
    }

    private fun addRecordList(data: RecordModel) {

        lifecycleScope.launch {
            saveRecordToDatabase(data)
            viewModel.recordList.add(0,data)
            recordAdapter.notifyItemInserted(0)
        }
    }

    override fun initView() {
        rvRecordList = findView(R.id.card_list)
        rvRecordList.layoutManager = LinearLayoutManager(context)
        recordAdapter = RecordAdapter(viewModel.recordList)
        rvRecordList.adapter = recordAdapter
        recordAdapter.onItemPlayListener = { audioPath ->
            val playFragment = PlayFragment().apply {
                arguments = Bundle().apply {
                    putString("KEY_AUDIO_PATH", audioPath)
                }
            }
            switchFragment(playFragment, addToBackStack = true)
        }
        recordAdapter.onItemDeleteListener = { position ->
            val recordToDelete = viewModel.recordList[position]
            lifecycleScope.launch {
                deleteRecordFromDatabase(recordToDelete)
                recordAdapter.removeItem(position)
                showToast(getString(R.string.record_delete))
            }
        }
        arguments?.let { bundle ->
            userModel = bundle.getSerializable("KEY_USER") as? UserModel
            currentUserId = userModel?.userId ?: 0
            val recordModel = bundle.getSerializable("KEY_RECORD_MODEL") as? RecordModel
            lifecycleScope.launch {
                loadUserRecords()
            }
            recordModel?.let {
                addRecordList(it)
                bundle.remove("KEY_RECORD_MODEL")
            }
        }
    }

    private suspend fun deleteRecordFromDatabase(recordModel: RecordModel) {
        if (recordModel.audioPath.isNullOrEmpty()) return

        val db = AudioManage.getInstance(requireContext())
        // 1. 先根据音频路径查数据库里的AudioRecord（需要给AudioDao加查询方法）
        val audioId = db.audioDao().getAudioIdByPath(recordModel.audioPath)
        // 2. 删除查到的录音
        if (audioId != null) {
            db.audioDao().deleteAudioById(audioId)
        }
    }

    private suspend fun loadUserRecords() {
        val db = AudioManage.getInstance(requireContext())
        val audioList = db.audioDao().getAudiosByUserId(currentUserId)
        val recordModels = audioList.map { audioRecord ->
            RecordModel(
                title = audioRecord.title,
                time = audioRecord.time,
                audioPath = audioRecord.audioPath
            )
        }

        viewModel.recordList.clear()
        viewModel.recordList.addAll(recordModels)
        recordAdapter.notifyDataSetChanged()

    }

    override fun initListener(){
        val btnGoOn: Button=findView(R.id.btn_goOn)
        val btnUserSetting: ImageButton=findView(R.id.btn_userSetting)

        btnGoOn.setOnClickListener {
            val recordFragment = RecordFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("KEY_USER", userModel)
                }
            }
            switchFragment(recordFragment)
        }
        btnUserSetting.setOnClickListener {
            userModel?.let { model ->
                val userSettingFragment = UserSettingFragment().apply {
                    val bundle = Bundle()
                    bundle.putSerializable("KEY_USER", model)
                    arguments = bundle
                }
                switchFragment(userSettingFragment)
            }
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
    private val POWER = 8888

    private var userModel: UserModel? = null

    override fun getLayoutId(): Int=R.layout.fragment_record
    private lateinit var tvTime: TextView
    private var totalTime = 0
    private var isTiming = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var audioLogic: AudioLogic
    private var isRecordCanceled = false
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
    private val timeRun = object : Runnable {
        override fun run() {
            if (isTiming) {
                totalTime++
                updateTimeDisplay()
                mainHandler.postDelayed(this, 1000)
            }
        }
    }

    private fun startRecording() {
        audioLogic = AudioLogic(requireContext())
        if (!audioLogic.hasRecordPermission()) {
            requestPermissions(
                arrayOf(android.Manifest.permission.RECORD_AUDIO), // 要申请的录音权限
                POWER
            )
            return
        }
        val isSuccess = audioLogic.startRecording()
        startTimer()
        if (!isSuccess) {
            stopTimer()
            requireActivity().onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == POWER) {
            if (grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                val isSuccess = audioLogic.startRecording()
                startTimer()
                if (!isSuccess) {
                    stopTimer()
                    requireActivity().onBackPressed()
                }
            } else {
                stopTimer()
                showToast(getString(R.string.record_power_no))
                Handler(Looper.getMainLooper()).postDelayed({
                    requireActivity().onBackPressed()
                }, 100)
            }
        }
    }

    private fun stopRecording() {
        if (isRecordCanceled) {
            audioLogic.cancelRecording()
        } else {
            audioLogic.stopRecording()
        }
    }

    override fun initView() {
        tvTime = findView(R.id.tv_time)
        tvTime.text = "00:00"
        arguments?.let { bundle ->
            userModel = bundle.getSerializable("KEY_USER") as? UserModel
        }
        startRecording()
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            isRecordCanceled = true
            stopTimer()
            stopRecording()
            isEnabled = false
        }
    }

    override fun initListener(){
        val btnYes: ImageButton =findView(R.id.btn_record)
        btnYes.setOnClickListener {
            stopTimer()
            stopRecording()
            val finalTime = formatTime(totalTime)
            val bundle = Bundle()
            bundle.putString("KEY_RECORD_TIME", finalTime)
            bundle.putString("KEY_RECORD_PATH", audioLogic.getCurrentFilePath())
            bundle.putSerializable("KEY_USER", userModel)
            val settingFragment = SettingRecordFragment().apply {
                arguments = bundle

            }
            switchFragment(settingFragment, addToBackStack = true)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // 停止计时
        stopTimer()
        mainHandler.removeCallbacksAndMessages(null)
        // 兜底释放录音资源
        if (::audioLogic.isInitialized && audioLogic.isRecording()) {
            audioLogic.onDestroy()
        }
    }
}

class SettingRecordFragment:BaseFragment(){
    override val TAG:String="SettingRecordFragment"

    override fun getLayoutId(): Int=R.layout.fragment_setting_record
    private lateinit var tvTime: TextView
    private lateinit var etRecordName: EditText
    private var finalTime: String = "00:00"
    private var audioPath: String? = null

    private var userModel: UserModel? = null

    override fun initView() {
        tvTime = findView(R.id.tv_time)
        etRecordName = findView(R.id.et_recordName)

        arguments?.let { bundle ->
            val time = bundle.getString("KEY_RECORD_TIME")
            if (!time.isNullOrEmpty()) {
                finalTime=time
                tvTime.text="$finalTime"
            }
            audioPath = bundle.getString("KEY_RECORD_PATH")
            userModel = bundle.getSerializable("KEY_USER") as? UserModel
        }
    }

    override fun initListener(){
        val btnYes: Button=findView(R.id.btn_account_new)
        val btnNo: Button=findView(R.id.btn_no)
        btnYes.setOnClickListener {
            val recordTitle = etRecordName.text.toString().trim()

            if (recordTitle.isEmpty()) {
                showToast(getString(R.string.record_title))
                return@setOnClickListener
            }
            val recordModel = RecordModel(
                time = finalTime,
                title = recordTitle,
                audioPath = audioPath
            )

            val actionFragment = ActionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("KEY_RECORD_MODEL", recordModel)
                    putSerializable("KEY_USER", userModel)
                }
            }
            switchFragment(actionFragment, addToBackStack = true)
        }
        btnNo.setOnClickListener{
            audioPath?.let { path ->
                val file = java.io.File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            val actionFragment = ActionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("KEY_USER", userModel)
                }
            }
            switchFragment(actionFragment)
        }
    }

}

class UserSettingFragment:BaseFragment(){
    override val TAG: String = "UserSettingFragment"

    override fun getLayoutId(): Int=R.layout.fragment_setting_user

    private lateinit var get_username:EditText
    private lateinit var get_userpwd: EditText

    private var original_username: String=""
    private var original_userpwd:String=""

    override fun initView() {
        get_username = findView(R.id.et_username)
        get_userpwd = findView(R.id.et_userpwd)

        arguments?.let { bundle ->
            val userModel = bundle.getSerializable("KEY_USER") as? UserModel
            userModel?.let {
                original_username=it.username
                original_userpwd=it.userpwd
                get_username.setText(it.username)
                get_userpwd.setText(it.userpwd)
            }
        }
    }

    override fun initListener(){
        val btnYes: Button=findView(R.id.btn_yes)
        val btnNo: Button=findView(R.id.btn_no)
        val btnDelete:Button=findView(R.id.btn_delete)

        btnYes.setOnClickListener{
            val judgmentName=get_username.text.toString().trim()
            val judgmentPwd=get_userpwd.text.toString().trim()

            val isName=judgmentName!=original_username
            val isPwd=judgmentPwd!=original_userpwd

            if (!isName&&!isPwd) {
                showToast(getString(R.string.reUserSetting))
                return@setOnClickListener
            } else {
                lifecycleScope.launch {
                    when {
                        isName && !isPwd -> {
                            UserManage.updateUsername(
                                requireContext(),
                                original_username,
                                judgmentName
                            )
                        }

                        !isName && isPwd -> {
                            UserManage.updatePassword(
                                requireContext(),
                                original_username,
                                judgmentPwd
                            )
                        }

                        else -> {
                            UserManage.updateUser(
                                requireContext(),
                                original_username,
                                judgmentName,
                                judgmentPwd
                            )
                        }
                    }
                    showToast(getString(R.string.reUpUser))
                    switchFragment(MainFragment())
                }
            }
        }
        btnNo.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStackImmediate()
            } else {
                switchFragment(ActionFragment())
            }
        }
        btnDelete.setOnClickListener{
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.log_out))
                .setMessage(getString(R.string.relog_out))
                .setPositiveButton(getString(R.string.btn_yes)) { dialog, _ ->
                    lifecycleScope.launch {
                        val user = withContext(Dispatchers.IO) {
                            UserManage.getUserDao(requireContext()).getUser(original_username)
                        }
                        if (user != null) {
                            val audioManage = AudioManage.getInstance(requireContext())
                            val allRecords = audioManage.audioDao().getAudiosByUserId(user.id)

                            audioManage.audioDao().deleteAudiosByUserId(user.id)

                            allRecords.forEach { record ->
                                record.audioPath?.let { path ->
                                    val file = java.io.File(path)
                                    if (file.exists()) {
                                        file.delete()
                                    }
                                }
                            }
                        }
                        UserManage.deleteUser(
                            requireContext(),
                            original_username
                        )
                        showToast(getString(R.string.log_out_success))
                        switchFragment(MainFragment())
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}

class PlayFragment : BaseFragment(){
    override val TAG: String = "PlayFragment"
    private lateinit var ivPlayer: ImageView
    private lateinit var btnReturn: Button
    private var audioPath: String? = null
    private var isPlayCompleted = false

    override fun getLayoutId(): Int = R.layout.fragment_play

    override fun initView() {
        ivPlayer = findView(R.id.iv_player)
        btnReturn = findView(R.id.btn_return)

        arguments?.let {
            audioPath = it.getString("KEY_AUDIO_PATH")
            audioPath?.let { path ->
                val isSuccess = AudioPlayer.playAudio(requireContext(), path)
                if (!isSuccess) {
                    requireActivity().onBackPressed()
                }else {
                    // ========== 新增：监听播放完成 ==========
                    AudioPlayer.setOnCompletionListener {
                        isPlayCompleted = true // 标记播放完成
                        ivPlayer.setImageResource(R.drawable.iv_pause)
                    }
                }
            }
        }
    }

    override fun initListener() {
        ivPlayer.setOnClickListener {
            if (AudioPlayer.isPlaying) {
                AudioPlayer.pauseAudio()
            } else {
                AudioPlayer.resumeAudio()
            }
            updatePlayerIcon()
        }

        btnReturn.setOnClickListener {
            AudioPlayer.stopAudio()
            requireActivity().onBackPressed()
        }
    }
    private fun updatePlayerIcon() {
        if (AudioPlayer.isPlaying) {
            ivPlayer.setImageResource(R.drawable.iv_player)
        } else {
            ivPlayer.setImageResource(R.drawable.iv_pause)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AudioPlayer.stopAudio()
    }
}