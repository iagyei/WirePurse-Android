package com.transcodium.tnsmoney

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.transcodium.tnsmoney.classes.Status
import com.transcodium.tnsmoney.classes.WalletCore
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

            assetId = assetInfo!!.getString("_id")

            chain = assetInfo!!.getString("chain")

            dialogTitle.text = mActivity.getString(R.string.send_space_asset, cryptoSymbol!!.toUpperCase())

            //close dialog
            closeModal.setOnClickListener { mActivity.finish() }

        }

    }//end onCreate


    /**
     * pageAdapter
     */
    inner class pageAdapter(fm: FragmentManager): FragmentPagerAdapter(fm){

        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

}//end class
