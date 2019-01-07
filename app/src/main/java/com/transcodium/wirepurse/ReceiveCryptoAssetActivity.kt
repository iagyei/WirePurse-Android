package com.transcodium.wirepurse

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.transcodium.wirepurse.classes.AppAlert
import com.transcodium.wirepurse.classes.Status
import com.transcodium.wirepurse.classes.WalletCore
import com.transcodium.wirepurse.db.entities.AssetAddress
import kotlinx.android.synthetic.main.activity_receive_crypto_asset.*
import kotlinx.android.synthetic.main.activity_send_crypto_asset.*
import kotlinx.android.synthetic.main.circular_progress_bar.*
import kotlinx.android.synthetic.main.dialog_header.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast
import org.json.JSONObject

class ReceiveCryptoAssetActivity : com.transcodium.wirepurse.ActivityDialogBase() {

    var cryptoSymbol: String? = null

    var assetId: String? = null

    var chain: String? = null

    var assetInfo: JSONObject? = null

    val mActivity by lazy { this }

    val clipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_crypto_asset)



        IO.launch {

            val data = intent.extras!!

            cryptoSymbol = data.getString("asset_symbol") ?: null

            if(cryptoSymbol == null){
                opError(Status.error(R.string.unknown_asset))
                return@launch
            }

            val assetInfoStatus = WalletCore.getAssetInfo(mActivity,cryptoSymbol!!)

            if(assetInfoStatus.isError()) {
                opError(assetInfoStatus)
            }

             assetInfo = assetInfoStatus.getData<JSONObject>()

            if(assetInfo == null){
                opError(); return@launch
            }

            assetId = assetInfo!!.getString("_id")

            chain = assetInfo!!.getString("chain")

            val hasPaymentId = assetInfo!!.optBoolean("has_payment_id",false)


            dialogTitle.text = mActivity.getString(R.string.receive_space_asset, cryptoSymbol!!.toUpperCase())

            val cryptoAddressesStatus = WalletCore.fetchDBAssetAddress(
                    mActivity,
                    assetId!!,
                    chain!!
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
            generateAddress()
        }//end on click

        //close dialog
        closeModal.setOnClickListener { mActivity.finish() }

    }//end fun



    /**
     * fetchAddress
     */
     private fun processAddressesUI(addressData: AssetAddress) = UI.launch{

        progressBar.hide()

        contentView.visibility = View.VISIBLE


        //lets get address
        val address = addressData.address

        //update address TextView
        addressTextField.text = address

        val addressDataUri = WalletCore.getChainPaymentUri(chain!!,address)

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

        if(isLoading){
            UI.launch { toast(R.string.app_loading_data) }
            return@launch
        }

        progressBar.show()

        isLoading = true

        val resultStatus = WalletCore.networkGenerateAddress(
                 mActivity,
                 assetId!!,
                 chain!!
        )

        if(resultStatus.isError()){
            isLoading = false
            opError(resultStatus)
            return@launch
        }

        val addressData = resultStatus.getData<AssetAddress>()

        if(addressData == null) {opError(); return@launch }

        isLoading = false

        processAddressesUI(addressData)
    }//end fun


}
