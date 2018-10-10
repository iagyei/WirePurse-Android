package com.transcodium.tnsmoney

import android.content.Intent
import android.os.Bundle
import com.transcodium.tnsmoney.classes.Status
import com.transcodium.tnsmoney.classes.ViewPagerAdapter
import com.transcodium.tnsmoney.classes.WalletCore
import kotlinx.android.synthetic.main.activity_send_crypto_asset.*
import kotlinx.android.synthetic.main.dialog_header.*
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject

class SendCryptoAssetActivity : ActivityDialogBase() {


    var cryptoSymbol: String? = null

    var assetId: String? = null

    var chain: String? = null

    var assetInfo: JSONObject? = null

    val mActivity by lazy { this }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_crypto_asset)


        IO.launch {

            val data = intent.extras!!

            cryptoSymbol = data.getString("asset_symbol") ?: null

            if (cryptoSymbol == null) {
                opError(Status.error(R.string.unknown_asset))
                return@launch
            }

            val assetInfoStatus = WalletCore.getAssetInfo(mActivity, cryptoSymbol!!)

            if (assetInfoStatus.isError()) {
                opError(assetInfoStatus)
            }

            assetInfo = assetInfoStatus.getData<JSONObject>()

            if (assetInfo == null) {
                opError(); return@launch
            }

            val assetId = assetInfo!!.getString("_id")

            val assetChain = assetInfo!!.getString("chain")

            val assetName = assetInfo!!.getString("name")

            dialogTitle.text = mActivity.getString(R.string.send_space_asset, cryptoSymbol!!.toUpperCase())


            UI.launch {

                //close dialog
                closeModal.setOnClickListener { mActivity.finish() }

                val adapter = ViewPagerAdapter(supportFragmentManager)

                adapter.addFragment(
                        SendCryptoAssetFragment.newInstance(
                                layoutId = R.layout.send_crypto_asset_external,
                                assetSymbol = cryptoSymbol!!,
                                assetId = assetId,
                                assetChain = assetChain
                        ),
                        mActivity.getString(R.string.send_to_address)
                )

                adapter.addFragment(

                        SendCryptoAssetFragment.newInstance(
                                layoutId = R.layout.send_crypto_asset_internal,
                                assetSymbol = cryptoSymbol!!,
                                assetId = assetId,
                                assetChain = assetChain
                        ),

                        mActivity.getString(R.string.send_to_user)
                )


                viewPager.adapter = adapter

                tabLayout.setupWithViewPager(viewPager)

            }//end ui operations

        }//end IO operation


    }//end onCreate

    /**
     * forward all onActivityResults to fragments
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val allFragments = supportFragmentManager.fragments

        for(fragment in allFragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * forward all onRequestPermissionsResult to fragments
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        val allFragments = supportFragmentManager.fragments

        for(fragment in allFragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}//end class
