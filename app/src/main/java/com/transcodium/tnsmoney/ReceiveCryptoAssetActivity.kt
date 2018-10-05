package com.transcodium.tnsmoney

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.transcodium.tnsmoney.classes.AppAlert
import com.transcodium.tnsmoney.classes.Status
import com.transcodium.tnsmoney.classes.WalletCore
import com.transcodium.tnsmoney.db.entities.AssetAddress
import kotlinx.android.synthetic.main.activity_receive_crypto_asset.*
import kotlinx.android.synthetic.main.circular_progress_bar.*
import kotlinx.android.synthetic.main.dialog_header.*
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast
import org.json.JSONObject

class ReceiveCryptoAssetActivity : ActivityDialogBase() {

    var cryptoSymbol: String? = null

    val mActivity by lazy { this }

    val clipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_crypto_asset)



        IO.launch {

            val data = intent.extras!!

            cryptoSymbol = data.getString("asset_symbol")

            dialogTitle.text = mActivity.getString(R.string.receive_space_asset, cryptoSymbol!!.toUpperCase())

            val cryptoAddressesStatus = WalletCore.fetchDBAssetAddress(
                    mActivity,
                    cryptoSymbol!!

            )

            if (cryptoAddressesStatus.isError()) {
                opError(cryptoAddressesStatus); return@launch
            }

            val addressesData = cryptoAddressesStatus.getData<AssetAddress>()

            if(addressesData == null){
                Log.e("EMPTY_DATA_RETURNED","EMPTY Data was returned")
                opError(); return@launch
            }

            //proccess UI
            processAddressesUI(addressesData)

        }//end coroutine

        //copy address
        copyAddressBtn.setOnClickListener { copyAddress() }

        addressFieldCard.setOnClickListener { copyAddress() }

        generateAddressBtn.setOnClickListener {

            progressBar.visibility = View.VISIBLE

            generateAddress()

            contentView.visibility = View.VISIBLE
        }//end on click

        //close dialog
        closeModal.setOnClickListener { mActivity.finish() }

    }//end fun

    /**
     * fetchAddress
     */
     private fun processAddressesUI(addressData: AssetAddress) = UI.launch{

        progressBar.visibility = View.GONE
        contentView.visibility = View.VISIBLE


        //lets get address
        val address = addressData.address

        //update address TextView
        addressTextField.text = address

        val addressDataUri = WalletCore.getAssetDataUri(cryptoSymbol!!,address)

        //generate qr code
        generateQRCode(addressDataUri, dip(280),qrCodeView)

    }//en fun


    /**
     * copyAddress
     */
    fun copyAddress(){

        val clipboardData = ClipData.newPlainText("text",addressTextField.text)

        clipboardManager.primaryClip = clipboardData

        toast(R.string.address_copied_to_clipboard)
    }//end copy address


    /**
     * generateAddress
     */
    private  fun generateAddress() = IO.launch{

        val resultStatus = WalletCore.networkGenerateAddress(mActivity,cryptoSymbol!!)

        if(resultStatus.isError()){
            opError(resultStatus)
            return@launch
        }

        val addressData = resultStatus.getData<AssetAddress>()

        if(addressData == null) {opError(); return@launch }

        processAddressesUI(addressData)
    }//end fun


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
