package com.example.stores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stores.databinding.ItemStoreBinding

class StoreAdapter(private var stores: MutableList<StoreEntity>, private var listener: OnClickListener) : RecyclerView.Adapter<StoreAdapter.ViewHolder>(){

    private lateinit var nContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        nContext = parent.context

        val view = LayoutInflater.from(nContext).inflate(R.layout.item_store, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val store = stores.get(position)

        with(holder){
            setListener(store)
            binding.textViewName.text = store.nombre
            binding.checkboxFavorite.isChecked = store.isFavorite

            Glide.with(nContext)
                .load(store.photoURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imagePhoto)
        }
    }

    override fun getItemCount(): Int = stores.size

    fun add(storeEntity: StoreEntity) {
        if(!stores.contains(storeEntity)){
            stores.add(storeEntity)
            notifyItemInserted(stores.size - 1)
        }
    }

    fun setStores(stores: MutableList<StoreEntity>) {
        this.stores = stores
        notifyDataSetChanged()
    }

    fun update(storeEntity: StoreEntity) {
        val index = stores.indexOf(storeEntity)
        if(index != -1){
            stores.set(index, storeEntity)
            notifyItemChanged(index)
        }
    }

    fun delete(storeEntity: StoreEntity) {
        val index = stores.indexOf(storeEntity)
        if(index != -1){
            stores.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemStoreBinding.bind(view)

        fun setListener(storeEntity: StoreEntity){
            with(binding.root){
                setOnClickListener{listener.onClick(storeEntity.id)}
                setOnClickListener{ listener.onDeleteStore(storeEntity)
                    true }
            }

            binding.checkboxFavorite.setOnClickListener{
                listener.onFavoriteStore(storeEntity)
            }
        }
    }
}