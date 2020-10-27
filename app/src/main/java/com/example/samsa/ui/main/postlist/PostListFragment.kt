package com.example.samsa.ui.main.postlist

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.samsa.R
import com.example.samsa.app.MyApplication
import com.example.samsa.databinding.FragmentPostListBinding
import com.example.samsa.tools.GridSpacingItemDecoration
import com.example.samsa.tools.adapters.TagListAdapter
import com.example.samsa.ui.main.MainActivity
import com.example.samsa.ui.main.MainViewModel
import com.example.samsa.ui.preference.SettingsActivity
import com.example.samsa.ui.util.WindowHelper
import kotlinx.android.synthetic.main.fragment_post_list.*
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [PostListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PostListFragment : Fragment(), TagListAdapter.TagListListener {

    private var favTagList = arrayListOf<String>()
    private lateinit var binding: FragmentPostListBinding
    private val mViewModel: MainViewModel by activityViewModels()

    companion object {
        @JvmStatic
        fun newInstance() = PostListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post_list, container, false)

        binding.vm = mViewModel
        binding.lifecycleOwner = this

        binding.menuButton.setOnClickListener {
            binding.postListDrawer.openDrawer(GravityCompat.START)
        }

        val postList = binding.postList

        postList.apply {
            addItemDecoration(
                GridSpacingItemDecoration(
                    2,
                    (10 * Resources.getSystem().displayMetrics.density).toInt(),
                    true,
                    0
                )
            )
            itemAnimator?.changeDuration = 0
        }
        binding.swipeRefreshLayout.setProgressViewOffset(
            true,
            (63 * Resources.getSystem().displayMetrics.density).toInt(),
            (100 * Resources.getSystem().displayMetrics.density).toInt()
        )

        // Preference button listener
        binding.settingsButton.setOnClickListener {
            if (isAdded) {
                val intent = Intent(activity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        binding.scrollUpFab.setOnClickListener {
            postList.smoothScrollToPosition(0)
        }

        binding.navigationContainer.setOnApplyWindowInsetsListener { v, insets ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                v.updatePadding(top = insets.getInsets(WindowInsets.Type.statusBars()).top)
            } else {
                v.updatePadding(top = insets.systemWindowInsetTop)
                insets.consumeSystemWindowInsets()
            }
            insets
        }

        mViewModel.mediaFragmentExitEvent.observe(viewLifecycleOwner, {
            if (!it.hasBeenHandled) {
                it.iHandledIt()
                activity?.window?.let { w ->
                    WindowHelper.exitFullscreenMode(w)
                }
                binding.postList.smoothScrollToPosition(mViewModel.postListPosition)
                refreshFavTagList()
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            val adapter = PostListAdapter(mViewModel)
            // *suppress recyclerview flickering
            adapter.setHasStableIds(true)
            post_list.adapter = adapter

            tag_searchBar.setAdapter(
                AutocompleteAdapter(
                    this@PostListFragment,
                    requireContext(),
                    R.layout.autocomplete_item
                )
            )

            // TODO : Fix favorite Tag List & Merge [Media]TagListAdapter
            favTagList = ArrayList(
                MyApplication.getInstance().getPref()
                    .getStringSet(getString(R.string.pref_key_favorite), setOf())!!
            )

            favorite_tag_listView.adapter =
                TagListAdapter(this@PostListFragment, requireContext(), favTagList)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (post_list != null)
            mViewModel.postListPosition =
                (post_list.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
        super.onSaveInstanceState(outState)
    }

    override fun onTagClick(tag: String) {
        startActivity(Intent(context, MainActivity::class.java).apply {
            putExtra("tagName", tag)
        })
    }

    override fun onTagLongClick(tag: String) {
        context?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("『$tag』를 즐겨찾기에서 제거?")
                setPositiveButton(R.string.ok) { _, _ ->
                    MyApplication.getInstance()
                        .removeTagFromPreferenceSet(tag, R.string.pref_key_favorite)
                    favTagList.remove(tag)
                    (favorite_tag_listView.adapter as TagListAdapter).notifyDataSetChanged()
                    Toast.makeText(it, "$tag 제거됨", Toast.LENGTH_SHORT).show()
                }
                setNeutralButton(R.string.cancel, null)
            }
            builder.create().show()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshFavTagList()
    }

    private fun refreshFavTagList() {
        val favTagAdaper = favorite_tag_listView.adapter
        if (favTagAdaper is TagListAdapter) {
            favTagAdaper.changeList(
                ArrayList(
                    MyApplication.getInstance().getPref()
                        .getStringSet(getString(R.string.pref_key_favorite), setOf())!!
                )
            )
        }
    }
}