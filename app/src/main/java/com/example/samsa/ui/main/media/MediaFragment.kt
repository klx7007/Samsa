package com.example.samsa.ui.main.media

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.samsa.R
import com.example.samsa.app.MyApplication
import com.example.samsa.databinding.FragmentMediaBinding
import com.example.samsa.ui.main.MainViewModel
import com.example.samsa.ui.main.OnBackPressedListener
import com.example.samsa.ui.util.ViewAnimation
import com.example.samsa.ui.util.WindowHelper
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.effet.RippleEffect
import com.takusemba.spotlight.shape.Circle
import kotlinx.android.synthetic.main.fragment_media.*
import kotlinx.android.synthetic.main.fragment_media.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val MY_PERMISSIONS_REQUEST_WRITE_DISK = 1

class MediaFragment : Fragment(), OnBackPressedListener {
    // TODO : think about lifecycle, in the corner crying
    private lateinit var binding: FragmentMediaBinding
    private val mViewModel: MainViewModel by activityViewModels()
    private val downloadManager by lazy {
        requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    lateinit var mediaViewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.window?.let {
            WindowHelper.fullscreenMode(it)
        }

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_media, container, false)

        binding.vm = mViewModel
        binding.lifecycleOwner = this

        setupViewPager(mViewModel)

        setupDownloadButton()

        binding.mediaBackButton.setOnClickListener {
            onBackPressed()
        }

        if (MyApplication.getInstance().getPref()
                .getBoolean(getString(R.string.pref_key_display_tutorial), false)
        ) {
            displaySpotlightTutorial(container)
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = MediaFragment()
    }

    private fun setupViewPager(vm: MainViewModel) {
        mediaViewPager = binding.mediaViewpager2

        mediaViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                vm.onPagerPageChange(position)
                binding.tagsList.scrollToPosition(0)
            }
        })

        mediaViewPager.offscreenPageLimit = MyApplication.getInstance().getPref()
            .getInt(getString(R.string.pref_key_preload_count), 1)
        mediaViewPager.setPageTransformer(MarginPageTransformer(resources.getDimensionPixelSize(R.dimen.media_viewpager2_gap)))
        mediaViewPager.adapter = MediaPagerAdapter(this)
        mediaViewPager.post { mediaViewPager.setCurrentItem(mViewModel.position, false) }
    }

    private fun setupDownloadButton() {
        binding.downloadButton.setOnClickListener {

            AlertDialog.Builder(requireContext()).apply {
                setMessage(getString(R.string.post_download_confirm))
                setPositiveButton(R.string.ok) { _, _ ->
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) { // 읽기/쓰기 권한 모두 있을 때
                        startDownload()
                    } else { // 권한 없을 때 권한 요청
                        this@MediaFragment.requestPermissions(
                            arrayOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ),
                            MY_PERMISSIONS_REQUEST_WRITE_DISK
                        )
                    }
                }
                setNeutralButton(R.string.cancel, null)
            }.create().show()
        }
    }

    private fun startDownload() {
        val post = mViewModel.postList.value?.get(mViewModel.position)
        if (post == null) {
            Toast.makeText(requireContext(), "다운로드 오류", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(
            requireContext(),
            getString(R.string.starting_post_download, post.postId),
            Toast.LENGTH_SHORT
        ).show()

        lifecycleScope.launch(Dispatchers.Main) {
            val mediaUrl = if (post.fileUrl !== null) {
                post.fileUrl
            } else {
                if (post.isVid) {
                    MyApplication.getInstance().getGallery().getVideoUrl(post.postId)
                } else {
                    MyApplication.getInstance().getGallery().getImageUrl(post.postId)
                }
            }

            val filename = post.postId + "." + MimeTypeMap.getFileExtensionFromUrl(mediaUrl)
            val request = DownloadManager.Request(Uri.parse(mediaUrl))
                .setTitle("$filename " + getString(R.string.download))
                .setDescription(getString(R.string.app_name))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, filename)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            downloadManager.enqueue(request)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            // setup tagList adapter
            binding.tagsList.adapter = MediaTagListAdapter(mViewModel)
        }
    }

    override fun onBackPressed() {
        if (tags_drawer_layout?.isDrawerOpen(GravityCompat.START) == true) {
            tags_drawer_layout.closeDrawer(GravityCompat.START, false)
        } else {
            mViewModel.onMediaFragmentExit()
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_DISK) {

            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {

                    AlertDialog.Builder(requireContext()).apply {
                        setMessage(getString(R.string.storage_permission_blocked))
                        setPositiveButton(R.string.ok) { _, _ ->
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${requireActivity().packageName}")
                            ).apply {
                                addCategory(Intent.CATEGORY_DEFAULT)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(this)
                            }
                        }
                        setNeutralButton(R.string.cancel, null)
                    }.create().show()

                    return
                }
            }

            startDownload()
        }
    }

    fun toggleControlButtons() {
        if (binding.controlsLayout.visibility == View.VISIBLE) {
            ViewAnimation.viewGoneAnimation(binding.controlsLayout)
        } else {
            binding.controlsLayout.alpha = 1.0f
            binding.controlsLayout.visibility = View.VISIBLE
        }
    }

    fun toggleControlButtons(show: Boolean) {
        if (show) {
            binding.controlsLayout.alpha = 1.0f
            binding.controlsLayout.visibility = View.VISIBLE
        } else {
            ViewAnimation.viewGoneAnimation(binding.controlsLayout)
        }
    }

    fun displaySpotlightTutorial(container: ViewGroup?) {
        val spotlightView = layoutInflater.inflate(R.layout.spotlight_tags, container, false)

        val target = Target.Builder()
            .setAnchor(-150.0f, requireContext().resources.displayMetrics.heightPixels / 2.0f)
            .setShape(Circle(300f))
            .setEffect(RippleEffect(300f, 350f, R.color.colorAccent))
            .setOverlay(spotlightView)
            .build()

        val spotlight = Spotlight.Builder(requireActivity())
            .setTargets(target)
            .setBackgroundColor(R.color.spotlight_background)
            .setDuration(1000L)
            .setAnimation(DecelerateInterpolator(2f))
            .setContainer(binding.root.pager_layout)
            .build()

        spotlightView.setOnClickListener {
            it.visibility = View.GONE
            MyApplication.getInstance().getPref().edit().apply {
                putBoolean(getString(R.string.pref_key_display_tutorial), false)
                apply()
            }
            MyApplication.getInstance().recachePref()
            spotlight.finish()
        }

        spotlight.start()
    }

}