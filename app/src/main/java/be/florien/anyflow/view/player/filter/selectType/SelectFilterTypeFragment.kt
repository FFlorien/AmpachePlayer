package be.florien.anyflow.view.player.filter.selectType

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.florien.anyflow.R
import be.florien.anyflow.databinding.FragmentSelectFilterBinding
import be.florien.anyflow.databinding.ItemSelectFilterTypeBinding
import be.florien.anyflow.di.ActivityScope
import be.florien.anyflow.di.UserScope
import be.florien.anyflow.view.player.PlayerActivity
import be.florien.anyflow.view.player.filter.BaseFilterFragment
import be.florien.anyflow.view.player.filter.BaseFilterVM
import be.florien.anyflow.view.player.filter.selection.SelectFilterFragment

@ActivityScope
@UserScope
class SelectFilterTypeFragment : BaseFilterFragment() {
    override fun getTitle(): String = getString(R.string.filter_title_main)
    override val baseVm: BaseFilterVM
        get() = vm
    lateinit var vm: AddFilterTypeFragmentVM
    private lateinit var fragmentBinding: FragmentSelectFilterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as PlayerActivity).activityComponent.inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        vm = AddFilterTypeFragmentVM(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentBinding = FragmentSelectFilterBinding.inflate(inflater, container, false)
        fragmentBinding.filterList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        fragmentBinding.filterList.adapter = FilterListAdapter()
        ResourcesCompat.getDrawable(resources, R.drawable.sh_divider, requireActivity().theme)?.let {
            fragmentBinding.filterList.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL).apply { setDrawable(it) })
            fragmentBinding.filterList.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.HORIZONTAL).apply { setDrawable(it) })
        }
        return fragmentBinding.root
    }

    inner class FilterListAdapter : RecyclerView.Adapter<FilterTypeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterTypeViewHolder = FilterTypeViewHolder()

        override fun onBindViewHolder(holder: FilterTypeViewHolder, position: Int) {
            holder.bind(vm.filtersIds[position], vm.filtersNames[position], vm.filtersImages[position])
        }

        override fun getItemCount(): Int = vm.filtersIds.size
    }

    inner class FilterTypeViewHolder(
            private val itemFilterTypeBinding: ItemSelectFilterTypeBinding = ItemSelectFilterTypeBinding.inflate(layoutInflater, fragmentBinding.filterList, false)
    ) : RecyclerView.ViewHolder(itemFilterTypeBinding.root) {

        fun bind(type: String, name: String, @DrawableRes drawableRes: Int) {
            itemFilterTypeBinding.filterName.text = name
            itemFilterTypeBinding.imageView.setImageResource(drawableRes)
            itemView.setOnClickListener {
                (activity as PlayerActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, SelectFilterFragment(type), SelectFilterFragment::class.java.simpleName)
                        .addToBackStack(null)
                        .commit()
            }
        }
    }
}
