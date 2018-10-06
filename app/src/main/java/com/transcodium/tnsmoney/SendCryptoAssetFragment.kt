package com.transcodium.tnsmoney


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


private const val LAYOUT_ID_PARAM = "layout_id"
private const val ASSET_SYMBOL_PARAM = "asset_symbol"
private const val ASSET_ID_PARAM = "asset_id"
private const val ASSET_CHAIN_PARAM = "asset_chain"


class SendCryptoAssetFragment : Fragment() {


    private var assetSymbol: String? = null
    private var assetId: String? = null
    private var assetChain: String? = null
    private var layoutId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        arguments?.let {
            assetSymbol = it.getString(ASSET_SYMBOL_PARAM)
            assetId = it.getString(ASSET_ID_PARAM)
            assetChain = it.getString(ASSET_CHAIN_PARAM)
            layoutId = it.getInt(LAYOUT_ID_PARAM)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(
                layoutId!!,
                container,
                false
        )
    }


    companion object {

        @JvmStatic
        fun newInstance(
                layoutId: Int,
                assetSymbol: String,
                assetId: String,
                assetChain: String
        ) = SendCryptoAssetFragment().apply {

                arguments = Bundle().apply {
                    putInt(LAYOUT_ID_PARAM,layoutId)
                    putString(ASSET_SYMBOL_PARAM, assetSymbol)
                    putString(ASSET_ID_PARAM, assetId)
                    putString(ASSET_CHAIN_PARAM, assetChain)
                }

        } //end apply

    }///end companion



}//end class
