package com.example.samsa.ui.main.media.pages

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.samsa.R
import com.example.samsa.app.MyApplication
import com.example.samsa.ui.main.MainViewModel
import com.example.samsa.ui.main.media.CacheDataSourceSingleton
import com.example.samsa.ui.main.media.MediaFragment
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_POST_ID = "postID"

// TODO: Fix video caching method
class VideoPageFragment : Fragment() {
    private var postID: String? = null
    private var player: SimpleExoPlayer? = null
    private var loadingPlayer = false

    private lateinit var progress: ProgressBar
    private lateinit var playerView: PlayerView
    private lateinit var errorText: TextView

    private lateinit var vidUrl: String
    private var urlLoaded = false

    private var lastPos: Long = 0
    private var mediaSource: MediaSource? = null

    private val mViewModel: MainViewModel by activityViewModels()

    private val parentMediaFragment by lazy {
        parentFragment as MediaFragment
    }

    companion object {
        @JvmStatic
        fun newInstance(postID: String) =
            VideoPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postID)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            postID = it.getString(ARG_POST_ID)
        }
        savedInstanceState?.getLong("lastPos")?.let {
            lastPos = it
        }
        savedInstanceState?.getString("vidUrl")?.let {
            vidUrl = it
        }

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_video_page, container, false)

        errorText = v.findViewById(R.id.video_error_text)
        progress = v.findViewById(R.id.video_progress)
        playerView = v.findViewById(R.id.video_playerView)

        playerView.setControllerVisibilityListener {
            if (it == View.GONE)
                parentMediaFragment.toggleControlButtons(false)
            else
                parentMediaFragment.toggleControlButtons(true)
        }

        if (postID == null) {
            showError(getString(R.string.video_fragment_null_id))
            return v
        }

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                if (!this@VideoPageFragment::vidUrl.isInitialized) {
                    vidUrl = mViewModel.postMapCache.value?.get(postID)?.fileUrl
                        ?: MyApplication.getInstance().getGallery().getVideoUrl(postID!!)
                }
                // if resumed and visible, load player
                if (player == null && isResumed)
                    loadPlayer()
            } catch (e: Exception) {
                showError(e.message ?: getString(R.string.source_loading_error))
            }
        }

        return v
    }

    override fun onResume() {
        if (player == null && this::vidUrl.isInitialized)
            loadPlayer()
        super.onResume()
    }

    override fun onPause() {
        player?.apply {
            playWhenReady = false
            lastPos = currentPosition
            release()
        }
        player = null

        super.onPause()
    }

    override fun onDestroy() {
        player?.release()
        player = null

        super.onDestroy()
    }

    private fun loadPlayer() {
        if (loadingPlayer)
            return
        else
            loadingPlayer = true

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                progress.visibility = View.VISIBLE
                player = withContext(Dispatchers.Default) {

                    val cacheDataFactory = CacheDataSourceSingleton.getInstance(requireContext())

                    mediaSource = ProgressiveMediaSource.Factory(cacheDataFactory)
                        .createMediaSource(Uri.parse(vidUrl))

                    val builder = DefaultLoadControl.Builder()
                    builder.setBufferDurationsMs(
                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                        0,
                        1024
                    )
                    val loadControl = builder.createDefaultLoadControl()

                    return@withContext SimpleExoPlayer.Builder(requireContext())
                        .setLoadControl(loadControl)
                        .build()
                }

                player?.apply {
                    prepare(mediaSource ?: throw Exception("Player source error"))
                    repeatMode = Player.REPEAT_MODE_ONE
                    playWhenReady = MyApplication.getInstance().getPref()
                        .getBoolean(getString(R.string.pref_key_video_autoplay), false)
                    seekTo(lastPos)
                    addListener(object : Player.EventListener {
                        override fun onPlayerStateChanged(
                            playWhenReady: Boolean,
                            playbackState: Int
                        ) {
                            if (playbackState == Player.STATE_READY) {
                                playerView.visibility = View.VISIBLE
                                progress.visibility = View.GONE
                            }

                            super.onPlayerStateChanged(playWhenReady, playbackState)
                        }
                    })
                }

                playerView.player = player

            } catch (e: Exception) {
                progress.visibility = View.GONE
                errorText.text = e.message
            } finally {
                loadingPlayer = false
            }
        }
    }

    private fun showError(message: String) {
        progress.visibility = View.GONE
        playerView.visibility = View.GONE

        errorText.text = message
        errorText.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // save url if loaded
        if (this::vidUrl.isInitialized) {
            outState.putString("vidUrl", vidUrl)
        }
        // save last position of video
        if (lastPos != 0L) {
            outState.putLong("lastPos", lastPos)
        }

        super.onSaveInstanceState(outState)
    }
}