package cn.wthee.signinsystem.ui.main

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Camera
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.camera.core.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import cn.wthee.signinsystem.R
import kotlinx.android.synthetic.*
import java.io.File
import java.util.*

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    //view
    private lateinit var mOpenCameraButton: Button
    private lateinit var mRecordStartButton: Button
    private lateinit var mRecordStopButton: Button
    private lateinit var mTextureView: TextureView
    private lateinit var mRecordTimeView: TextView

    //camera
    private lateinit var videoCapture: VideoCapture

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.main_fragment, container, false)

        mOpenCameraButton = rootView.findViewById(R.id.open_camera)
        mRecordStartButton = rootView.findViewById(R.id.record_start)
        mRecordStopButton = rootView.findViewById(R.id.record_stop)
        mTextureView = rootView.findViewById(R.id.preview)
        mRecordTimeView = rootView.findViewById(R.id.record_time)

        //打开或关闭相机
        mOpenCameraButton.setOnClickListener {
            if(this::videoCapture.isInitialized && CameraX.isBound(videoCapture)){
                closeCamera()
            }else{
                openCamera()
            }
        }
        //录制视频
        mRecordStartButton.setOnClickListener {
            startRecord()
        }
        //录制完成
        mRecordStopButton.setOnClickListener {
            videoCapture.stopRecording()
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    //打开相机
    @SuppressLint("RestrictedApi")
    private fun openCamera() {
        mOpenCameraButton.text = resources.getText(R.string.close_camera)
        mRecordStartButton.visibility = View.VISIBLE
        mRecordStopButton.visibility = View.VISIBLE
        //获取手机屏幕宽度
        val outMetrics =  DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(outMetrics)
        val screenWidth = outMetrics.widthPixels
        //修改预览高度
        val params = mTextureView.layoutParams
        params.height = (screenWidth / 3f * 4f).toInt()
        mTextureView.layoutParams = params
        //预览配置
        val previewConfig = PreviewConfig.Builder()
            .setLensFacing(CameraX.LensFacing.FRONT)
            .setTargetResolution(Size(screenWidth,(screenWidth / 3f * 4f).toInt()))
            .build()
        //录制配置
        val videoConfig = VideoCaptureConfig.Builder()
            .setLensFacing(CameraX.LensFacing.FRONT)
            .build()

        val preview = Preview(previewConfig)
        videoCapture = VideoCapture(videoConfig)
        //预览
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            val parent = mTextureView.parent as ViewGroup
            parent.removeView(mTextureView)
            mTextureView.surfaceTexture = previewOutput.surfaceTexture
            mTextureView.visibility = View.VISIBLE
            parent.addView(mTextureView, 0)
        }

        CameraX.bindToLifecycle(this as LifecycleOwner, videoCapture, preview)
    }

    //关闭相机
    private fun closeCamera(){
        CameraX.unbindAll()
        mOpenCameraButton.text = resources.getText(R.string.open_camera)
        mTextureView.visibility = View.GONE
        mRecordStartButton.visibility = View.GONE
        mRecordStopButton.visibility = View.GONE
    }

    @SuppressLint("RestrictedApi")
    private fun startRecord() {
        val saveFile = File(activity?.externalMediaDirs?.first(),"${System.currentTimeMillis()}.mp4");
        //录制计时
        var startTime = 0
        val task = object :TimerTask(){
            override fun run() {
                startTime += 1
                activity?.runOnUiThread {
                    mRecordTimeView.text = "已录制 ${startTime} s"
                }
            }
        }
        Timer().schedule(task,1000,1000)
        //开始录制
        videoCapture.startRecording(saveFile, {
            //结束录制
            task.cancel()
            activity?.runOnUiThread {
                mRecordTimeView.text = ""
            }
        }, object : VideoCapture.OnVideoSavedListener {
            override fun onVideoSaved(file: File) {
                Log.e("video", "save")
            }

            override fun onError(
                videoCaptureError: VideoCapture.VideoCaptureError,
                message: String,
                cause: Throwable?
            ) {
                Log.e("video", "error")
            }
        })
    }

}
