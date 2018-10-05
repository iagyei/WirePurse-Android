package com.transcodium.tnsmoney

import android.os.Bundle
import android.util.Log
import android.view.View
import com.transcodium.tnsmoney.classes.AppAlert
import com.transcodium.tnsmoney.classes.Status
import com.transcodium.tnsmoney.classes.WalletCore
import com.transcodium.tnsmoney.db.entities.AssetAddresses
import kotlinx.android.synthetic.main.activity_receive_crypto_asset.*
import kotlinx.android.synthetic.main.circular_progress_bar.*
import kotlinx.android.synthetic.main.dialog_header.*
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.dip
import org.json.JSONObject

class ReceiveCryptoAssetActivity : ActivityDialogBase() {

    var cryptoSymbol: String? = null

    val mActivity by lazy { this }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_crypto_asset)



        IO.launch {

            val data = intent.extras!!

            cryptoSymbol = data.getString("asset_symbol")

            dialogTitle.text = mActivity.getString(R.string.receive_space_asset, cryptoSymbol!!.toUpperCase())

            val cryptoAddressesStatus = WalletCore.fetchDBAssetAddresses(
                    mActivity,
                    cryptoSymbol!!

            )

            if (cryptoAddressesStatus.isError()) {
                opError(cryptoAddressesStatus); return@launch
            }

            val addressesData = cryptoAddressesStatus.getData<List<AssetAddresses>>()

            if(addressesData == null){
                Log.e("EMPTY_DATA_RETURNED","EMPTY Data was returned")
                opError(); return@launch
            }

            //proccess UI
            processAddressesUI(addressesData)

        }//end coroutine

            closeModal.setOnClickListener { mActivity.finish() }

    }

    /**
     * fetchAddress
     */
     fun processAddressesUI(addressesData: List<AssetAddresses>) = UI.launch{

        progressBar.visibility = View.GONE
        contentView.visibility = View.VISIBLE

        //latest addrress
        val latestAddressData = addressesData.last()

        //lets get address
        val latestAddress = latestAddressData.address

        //generate qr code
        generateQRCode(latestAddress, dip(280),qrCodeView)

        //update address TextView
        addressField.setText(latestAddress)

    }//en fun


    /**
     * error
     */
    fun opError(status: Status? = null) = UI.launch{

        val alertData = if(status != null){
            status
        }else{
            Status.error(R.string.unexpected_error)
        }

        progressBar.visibility = View.GONE
        AppAlert(mActivity).showStatus(alertData)
    }

}
