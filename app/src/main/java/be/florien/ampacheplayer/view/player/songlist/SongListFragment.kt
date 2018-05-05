package be.florien.ampacheplayer.view.player.songlist

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import be.florien.ampacheplayer.BR
import be.florien.ampacheplayer.R
import be.florien.ampacheplayer.databinding.FragmentSongListBinding
import be.florien.ampacheplayer.databinding.ItemSongBinding
import be.florien.ampacheplayer.di.ActivityScope
import be.florien.ampacheplayer.extension.GlideApp
import be.florien.ampacheplayer.persistence.model.Song
import be.florien.ampacheplayer.player.PlayerService
import be.florien.ampacheplayer.view.player.PlayerActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import javax.inject.Inject

/**
 * Display a list of accounts and play it upon selection.
 */
@ActivityScope
class SongListFragment : Fragment() {

    @Inject
    lateinit var vm: SongListFragmentVm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_song_list, container, false)
        val binding = DataBindingUtil.bind<FragmentSongListBinding>(view)
        (activity as PlayerActivity).activityComponent.inject(this)
        binding?.vm = vm
        vm.refreshSongs()
        binding?.songList?.adapter = SongAdapter().apply { songs = vm.getCurrentAudioQueue() }
        binding?.songList?.layoutManager = LinearLayoutManager(activity)
        requireActivity().bindService(Intent(requireActivity(), PlayerService::class.java), vm.connection, Context.BIND_AUTO_CREATE)
        vm.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, id: Int) {
                when (id) {
                    BR.currentAudioQueue -> (binding?.songList?.adapter as SongAdapter).songs = vm.getCurrentAudioQueue()
                    BR.listPosition -> {
                        val songAdapter = binding?.songList?.adapter as? SongAdapter
                        songAdapter?.notifyItemChanged(songAdapter.lastPosition)
                        songAdapter?.notifyItemChanged(vm.getListPosition())
                        songAdapter?.lastPosition = vm.getListPosition()
                        binding?.songList?.scrollToPosition(vm.getListPosition())
                    }
                }
            }
        })
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_song_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.refresh) {
            vm.refreshSongs()
            true
        } else {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.destroy()
        requireActivity().unbindService(vm.connection)
    }

    inner class SongAdapter : RecyclerView.Adapter<SongViewHolder>() {
        var songs = listOf<Song>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }
        var lastPosition = 0

        override fun getItemCount() = songs.size

        override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
            holder.isCurrentSong = position == vm.getListPosition()
            holder.bind(songs[position], position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SongViewHolder(parent)
    }

    inner class SongViewHolder(
            parent: ViewGroup,
            private val binding: ItemSongBinding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, position: Int) {
            binding.song = song
            binding.position = position
            binding.vm = vm
            GlideApp.with(binding.root)
                    .load(song.art)
                    .placeholder(R.drawable.cover_placeholder)
                    .error(R.drawable.cover_placeholder)
                    .fitCenter()
                    .into(binding.cover)
        }

        var isCurrentSong: Boolean = false
            set(value) {
                field = value
                val backgroundColor = if (field) R.color.selected else R.color.unselected
                binding.root.setBackgroundColor(ResourcesCompat.getColor(resources, backgroundColor, null))
            }
    }
}