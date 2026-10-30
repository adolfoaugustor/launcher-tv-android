package br.tematv.launcher.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.tematv.launcher.R

/** Grade de apps: um [ThemedAppTileView] por app, todos com a mesma moldura e cores do tema. */
class AppGridAdapter(
    private val onAppSelected: (AppEntry) -> Unit,
) : RecyclerView.Adapter<AppGridAdapter.TileHolder>() {

    class TileHolder(val tile: ThemedAppTileView) : RecyclerView.ViewHolder(tile)

    var entries: List<AppEntry> = emptyList()
        private set

    var style: TileStyle? = null
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    init {
        setHasStableIds(true)
    }

    fun submit(newEntries: List<AppEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    fun positionOf(packageName: String): Int = entries.indexOfFirst { it.packageName == packageName }

    override fun getItemCount(): Int = entries.size

    override fun getItemId(position: Int): Long =
        entries[position].app.let { "${it.packageName}/${it.activityName}" }.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileHolder {
        val resources = parent.resources
        val tile = ThemedAppTileView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.tile_width),
                resources.getDimensionPixelSize(R.dimen.tile_height),
            ).apply {
                val horizontal = resources.getDimensionPixelSize(R.dimen.tile_margin_horizontal)
                val vertical = resources.getDimensionPixelSize(R.dimen.tile_margin_vertical)
                setMargins(horizontal, vertical, horizontal, vertical)
            }
        }
        val holder = TileHolder(tile)
        tile.setOnClickListener {
            holder.bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { onAppSelected(entries[it]) }
        }
        return holder
    }

    override fun onBindViewHolder(holder: TileHolder, position: Int) {
        holder.tile.style = style
        holder.tile.entry = entries[position]
    }
}
