package com.transcodium.tnsmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.crypto_asset_select_dropdown.view.*
import kotlinx.android.synthetic.main.dialog_header.view.*
import org.json.JSONObject


private const val ASSET_SYMBOL_PARAM = "asset_symbol_param"


class DepositCryptoAsset : DialogBase() {


    private var assetSymbol: String? = null
    private var userAssets: MutableList<JSONObject>? = null
    private var selectedAssetInfo: JsonObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            assetSymbol = it.getString(ASSET_SYMBOL_PARAM)
        }

        setStyle(DialogFragment.STYLE_NORMAL,R.style.AppDialog)

        retainInstance = true

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_deposit_crypto_asset, container, false)


        rootView.closeModal.setOnClickListener { dismissAllowingStateLoss() }

        rootView.dialogTitle.text = getString(R.string.receive_space_asset,assetSymbol?.toUpperCase())

        val assetsSpinner = rootView.cryptoAssetSpinner



        return rootView
    }


    //get User Asset
    suspend fun fetchAssets(assetSymbol: String): MutableList<JSONObject>{

        if(userAssets != null){
            return userAssets!!
        }


    }


    companion object {
        @JvmStatic
        fun newInstance(assetSymbol : String) =
                DepositCryptoAsset().apply {
                    arguments = Bundle().apply {
                        putString(ASSET_SYMBOL_PARAM, assetSymbol)
                    }
                }
    }


}
