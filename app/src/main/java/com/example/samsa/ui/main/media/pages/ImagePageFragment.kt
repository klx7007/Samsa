package com.example.samsa.ui.main.media.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.samsa.R
import com.example.samsa.app.MyApplication
import com.example.samsa.tools.glide.GlideLoupeImageLoader
import com.example.samsa.ui.main.MainViewModel
import com.example.samsa.ui.main.media.MediaFragment
import com.igreenwood.loupe.Loupe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private const val ARG_POST_ID = "postID"

class ImagePageFragment : Fragment() {
    private lateinit var errorText: TextView
    private lateinit var progress: ProgressBar
    private lateinit var photoView: ImageView

    private var postID: String? = null
    private var glil: GlideLoupeImageLoader? = null
    private lateinit var imgUrl: String

    private var loadImageJob: Job? = null


    private val mViewModel: MainViewModel by activityViewModels()

    private val parentMediaFragment by lazy {
        parentFragment as MediaFragment?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            postID = it.getString(ARG_POST_ID)
        }

        savedInstanceState?.getString("imgUrl")?.let {
            imgUrl = it
        }

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_image_page, container, false)

        errorText = v.findViewById(R.id.image_errorText)
        progress = v.findViewById(R.id.image_progress)
        val container = v.findViewById<ViewGroup>(R.id.image_container)

        photoView = v.findViewById<ImageView>(R.id.image_photoView)

        // Show error test when id null
        if (postID == null) {
            showError(getString(R.string.image_fragment_null_id))
            return v
        }

        loadImageJob = lifecycleScope.launch(Dispatchers.Main) {
            try {
                if (!this@ImagePageFragment::imgUrl.isInitialized) {
                    imgUrl = mViewModel.postMapCache.value?.get(postID)?.fileUrl
                        ?: MyApplication.getInstance().getGallery().getImageUrl(postID!!)
                }
                glil = GlideLoupeImageLoader(
                    photoView,
                    container,
                    progress,
                    errorText,
                    loupeInitializer
                ).also {
                    it.load(imgUrl)
                }
            } catch (e: Exception) {
                showError(e.message ?: getString(R.string.source_loading_error))
            }
        }

        return v
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (this@ImagePageFragment::imgUrl.isInitialized) {
            outState.putString("imgUrl", imgUrl)
        }

        super.onSaveInstanceState(outState)
    }

    companion object {
        @JvmStatic
        fun newInstance(postID: String) =
            ImagePageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postID)
                }
            }
    }

    private fun showError(message: String) {
        progress.visibility = View.GONE
        photoView.visibility = View.GONE

        errorText.text = message
        errorText.visibility = View.VISIBLE
    }

    override fun onResume() {
        if (glil?.imgLoaded == false) {
            parentMediaFragment?.toggleControlButtons(true)
        }
        super.onResume()
    }

    override fun onPause() {
        glil?.resetImagePosition()
        super.onPause()
    }

    val loupeInitializer: (iv: ImageView, vg: ViewGroup) -> Loupe = { iv, vg ->
        Loupe.create(iv, vg) {
            minimumFlingVelocity = 1000.0f
            viewDragFriction = 0.5f
            dragDismissDistanceInDp = 1
            scaleAnimationDuration = 250L
            onViewTranslateListener = object : Loupe.OnViewTranslateListener {
                override fun onStart(view: ImageView) {}

                override fun onViewTranslate(view: ImageView, amount: Float) {
                    parentMediaFragment?.mediaViewPager?.isUserInputEnabled =
                        amount.absoluteValue <= 0.01f
                }

                override fun onRestore(view: ImageView) {
                    parentMediaFragment?.mediaViewPager?.isUserInputEnabled = true
                }

                override fun onDismiss(view: ImageView) {
                    parentMediaFragment?.onBackPressed()
                }

                override fun onSingleTap() {
                    parentMediaFragment?.toggleControlButtons()
                }
            }
        }
    }

    override fun onDestroyView() {
        glil?.cleanup()
        loadImageJob?.cancel()
        super.onDestroyView()
    }
}