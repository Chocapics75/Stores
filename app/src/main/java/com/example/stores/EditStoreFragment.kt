package com.example.stores

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stores.databinding.FragmentEditStoreBinding
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.LinkedBlockingQueue


class EditStoreFragment : Fragment() {

    private lateinit var mBinding: FragmentEditStoreBinding
    private var mActivity: MainActivity? = null
    private var mIsEditMode: Boolean = false
    private var mStoreEntity: StoreEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentEditStoreBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getLong(getString(R.string.arg_id), 0)
        if(id != null && id != 0L){
            mIsEditMode = true
            getStore(id)
        }else{
            mIsEditMode = false
            mStoreEntity = StoreEntity(nombre = "", phone = "", photoURL = "")
        }

        mActivity = activity as? MainActivity
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity?.supportActionBar?.title = getString(R.string.edit_store_title_add)

        setHasOptionsMenu(true)

        mBinding.etPhotoURL.addTextChangedListener {
            Glide.with(this)
                .load(mBinding.etPhotoURL.text.toString())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(mBinding.imgPhoto)
        }
    }

    private fun getStore(id: Long) {
        val queue = LinkedBlockingQueue<StoreEntity?>()
        Thread{
            mStoreEntity = StoreApplication.database.storeDao().getStoreById(id)
            queue.add(mStoreEntity)
        }.start()
        queue.take()?.let {
            setUiStore(it)
        }
    }

    private fun setUiStore(it: StoreEntity) {
        with(mBinding){
            /*etName.setText(it.nombre)
            etPhone.setText(it.phone)
            etWebSite.setText(it.website)
            etPhotoURL.setText(it.photoURL)*/
            etName.text = it.nombre.editable()
            etPhone.text = it.phone.editable()
            etWebSite.text = it.website.editable()
            etPhotoURL.text = it.photoURL.editable()
            Glide.with(requireActivity()).load(it.photoURL).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(imgPhoto)
        }
    }

    private fun String.editable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mActivity?.onBackPressed()
                true
            }
            R.id.action_save -> {
                if(mStoreEntity != null){
                    /*val store = StoreEntity(
                    nombre = mBinding.etName.text.toString().trim(),
                    phone = mBinding.etPhone.text.toString().trim(),
                    website = mBinding.etWebSite.text.toString().trim(),
                    photoURL = mBinding.etPhotoURL.text.toString().trim()
                )*/
                    with(mStoreEntity!!){
                        nombre = mBinding.etName.text.toString().trim()
                        phone = mBinding.etPhone.text.toString().trim()
                        website = mBinding.etWebSite.text.toString().trim()
                        photoURL = mBinding.etPhotoURL.text.toString().trim()
                    }
                    doAsync {
                        if(mIsEditMode)
                            StoreApplication.database.storeDao().updateStore(mStoreEntity!!)
                        else
                            mStoreEntity!!.id = StoreApplication.database.storeDao().addStore(mStoreEntity!!)
                        uiThread {
                            mActivity?.addStore(mStoreEntity!!)
                            ocultarTeclado()
                            /**
                             * Con Snackbar se puede solapar encima de botones o de información debajo de pantalla
                             */
                            /*Snackbar.make(
                                mBinding.root,
                                getString(R.string.edit_store_mensaje_correcto),
                                Snackbar.LENGTH_SHORT
                            ).show()*/
                            /**
                             * Con Toast no necesita vista y no solapa nada en la parte inferior
                             */
                            Toast.makeText(mActivity, R.string.edit_store_mensaje_correcto, Toast.LENGTH_SHORT).show()
                            mActivity?.onBackPressed()
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun ocultarTeclado(){
        val inm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onDestroyView() {
        ocultarTeclado()
        super.onDestroyView()
    }

    override fun onDestroy() {
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.app_name)
        mActivity?.hideFab(true)

        setHasOptionsMenu(false)
        super.onDestroy()
    }
}