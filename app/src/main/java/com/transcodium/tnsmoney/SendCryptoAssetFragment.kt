package com.transcodium.tnsmoney


import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.zxing.integration.android.IntentIntegrator
import com.transcodium.tnsmoney.classes.*
import kotlinx.android.synthetic.main.activity_send_crypto_asset.*
import kotlinx.android.synthetic.main.circular_progress_bar.*
import kotlinx.android.synthetic.main.send_crypto_asset_external.*
import kotlinx.android.synthetic.main.send_crypto_asset_external.view.*
import kotlinx.android.synthetic.main.send_crypto_asset_internal.*
import kotlinx.android.synthetic.main.send_crypto_asset_internal.view.*
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.net.URL


private const val LAYOUT_ID_PARAM = "layout_id"
private const val ASSET_INFO_PARAM = "asset_symbol"



class SendCryptoAssetFragment : Fragment() {

    private var assetInfo: JSONObject? = null
    private var assetSymbol: String? = null
    private var assetId: String? = null
    private var assetChain: String? = null
    private var layoutId: Int? = null
    private var hasPaymentId: Boolean = false
    private  val APP_CAMERA_PERMISSION = 15

    val mProgress by lazy{ Progress(mActivity!!) }

    //dataPair prepared for sending
    var proccessedDataToSend : MutableList<Pair<String,Any>>? = mutableListOf()


    val mActivity by lazy{
        this.activity
    }

    val barCodeScanner by lazy {
        IntentIntegrator.forSupportFragment(this)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setOrientationLocked(true)
                .setBeepEnabled(true)
                .setPrompt(getString(R.string.scanner_toggle_torch))
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        arguments?.let {

            layoutId = it.getInt(LAYOUT_ID_PARAM)

            assetInfo = JSONObject(it.getString(ASSET_INFO_PARAM))

            assetSymbol = assetInfo!!.optString("symbol")

            assetId = assetInfo!!.getString("_id")

            assetChain = assetInfo!!.getString("chain")

            hasPaymentId = assetInfo!!.optBoolean("has_payment_id",false)
        }


        //listen to viewPage changes and clear the processedDataToSend
        //to avoid mutiple requests
        mActivity?.viewPager?.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{

            override fun onPageScrollStateChanged(state: Int) {
                println("-----CLearing Data 1")
                proccessedDataToSend = null
            }

            override fun onPageSelected(position: Int) {
                println("-----CLearing Data 2")
                proccessedDataToSend = null
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        })//end page  changes

    }//end fun

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val rootView =  inflater.inflate(
                layoutId!!,
                container,
                false
        )

        //hide scan message if no camera
        if(!mActivity!!.hasCamera()){
            mActivity!!.scanWithCamera?.visibility = View.GONE
        }

        rootView.scanWithCamera?.setOnClickListener { openQRCodeScanner() }

        val chainName = WalletCore.getChainName(assetChain!!).capitalize()

        val chainNameAndSymbol = "$chainName (${assetChain?.toUpperCase()})"


        //external address input hint
        rootView?.externalAddressToSendInputLayout?.hint = getString(
                R.string.asset_name_space_address,chainNameAndSymbol
        )

        //if chain has payment id or destination tag requirement, lets make the input visible
        if(hasPaymentId){
            rootView?.externalpaymetIdInputInputLayout?.visibility = View.VISIBLE
        }


        //process send External
        rootView?.externalSendBtn?.setOnClickListener { processSendExternal() }

        rootView?.internalSendBtn?.setOnClickListener { processSendInternal() }

        return rootView
    }//end fun

    /**
     * qrCodeScanner
     */
    private fun openQRCodeScanner() {

        //lets ceck if app has permssion to camera
       val camPerm = ContextCompat.checkSelfPermission(mActivity!!, Manifest.permission.CAMERA)

        //if we have camera permission, then scan
        if(camPerm == PackageManager.PERMISSION_GRANTED) {
            barCodeScanner.initiateScan()
        }else{

           val alert = AlertDialog.Builder(mActivity!!,R.style.Theme_AppCompat_Light_Dialog)
                        .setMessage(R.string.camera_permission_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok){dialog, _ ->

                            //request permission
                            ActivityCompat.requestPermissions(
                                    mActivity!!,
                                    arrayOf( Manifest.permission.CAMERA),
                                    APP_CAMERA_PERMISSION
                            )
            }

            alert.show()
        }
    }//end


    /**
     * processSendInternal
     */
    fun processSendInternal(){

        proccessedDataToSend = null

        //lets proccess the data
        val recipientEmailAddress = internalReciepientEmailInput.text.toString()

        val amountToSend = internalAmountToSendInput.text.toString().toDoubleOrNull()

        internalReciepientEmailInputLayout.error = ""

        internalAmountToSendInputLayout.error = ""

        var hasError = false

        if(!recipientEmailAddress.isValidEmail()){
            internalReciepientEmailInputLayout.error = getString(R.string.invalid_email_address)
            hasError = true
        }

        if(amountToSend == null || amountToSend <= 0){
            internalAmountToSendInputLayout.error = getString(R.string.invalid_amount_to_send)
            hasError = true
        }

        if(hasError){ return }


        //lets show send confirmation
        showSendConfirmation(
                sendMode = "internal",
                address = recipientEmailAddress,
                amount = amountToSend!!
        )

    }//end fun



    /**
     * processSendExternal
     */
    fun processSendExternal(){

        proccessedDataToSend = null

        //lets proccess the data
        val externalAddress = externalAddressToSendInput.text.toString()

        val amountToSend = externalAmountToSendInput.text.toString().toDoubleOrNull()

        externalAddressToSendInputLayout.error = ""

        externalAmountToSendInputLayout.error = ""

        var hasError = false

        if(!CryptoAddress.isValid(assetChain!!,externalAddress)){
            externalAddressToSendInputLayout.error = getString(R.string.invalid_crypto_address,assetChain?.toUpperCase())
            hasError = true
        }


        if(amountToSend == null || amountToSend <= 0){
            externalAmountToSendInputLayout.error = getString(R.string.invalid_amount_to_send)
            hasError = true
        }

        if(hasError){ return }

        val paymentId: String? = externalpaymetIdInput.text?.toString()

        //lets show send confirmation
        showSendConfirmation(
                sendMode = "external",
                address = externalAddress,
                amount = amountToSend!!,
                paymentId = paymentId
        )
    }//end fun


    /**
     * sendConfrimation
     */
    fun showSendConfirmation(
            sendMode: String,
            address: String,
            amount: Double,
            paymentId: String? = null
    ){

        //clear data
        proccessedDataToSend = null

        val sendModeText = if(sendMode == "internal"){
            getString(R.string.internal)
        }else{
            getString(R.string.external)
        }

        var dialogContent = """
            <div>${getString(R.string.amount)} : $amount ${assetSymbol!!.toUpperCase()}</div>
             <div>${getString(R.string.send_mode)} : ${sendModeText.capitalize()}</div>
            <div>${getString(R.string.recipient_address)} : $address</div>
        """.trimIndent()

        if(paymentId != null){
            dialogContent += "<div>${getString(R.string.payment_id_or_destination_tag)} : $paymentId</div>"
        }

        mActivity!!.dialog {
            setCancelable(false)
            setTitle(R.string.confirm_transfer)
            setMessage(Html.fromHtml(dialogContent))
            setNegativeButton(R.string.cancel){d,_-> d.cancel() }
            setPositiveButton(R.string.confirm){d,_->

                proccessedDataToSend =  mutableListOf()

                //lets process the data
                proccessedDataToSend!!.apply {
                        add(Pair("send_mode", sendMode))
                        add(Pair("amount", amount))
                        add(Pair("recipient_address", address))
                        add(Pair("payment_id", paymentId ?: ""))
                        add(Pair("asset", assetSymbol!!))
                        add(Pair("asset_id", assetId!!))
                }

                mActivity!!.startInAppAuth()
            }
        }

    }//end fun


    /**
     *  sendAssetTransferToServer()
     */
    fun  sendAssetTransferToServer() = IO.launch{

        //avoid duplicate requests
        //since viewPage changes listener clears data, any attempt to
        //send mutiple request will cause it to be ignored
        //clear proccessedDataToSend after request
        if(proccessedDataToSend == null || proccessedDataToSend!!.isEmpty()){
            return@launch
        }

        mProgress.show(
           bgColor = R.color.purpleDarken2,
           dismissable = false,
           blockUI = true
        )

       println("-------DATA $proccessedDataToSend")

        val sendStatus = TnsApi(mActivity!!)
                    .post(
                            requestPath = "wallet/withdraw",
                            params = proccessedDataToSend
                    )


        mProgress.hide()

        //clear proccessedDataToSend
        proccessedDataToSend = null 

        if(sendStatus.isError()){
             AppAlert(mActivity!!).showStatus(sendStatus)
             return@launch
        }



    }//end fun

    /**
     * parse And Proccess QR Data
     */
     fun processQRData(data: String){

        val requiredScheme = WalletCore.getChainName(assetChain!!)

      //parse uri
       val uri = Uri.parse(data)

       val scheme = uri.scheme

       val queryString = uri.query

       var paymentAddress = data

       var label = ""

       var amount = 0.0


        if(scheme != null && requiredScheme != scheme){
            AppAlert(mActivity!!)
                    .error(
                            getString(R.string.invalid_qr_code,requiredScheme),
                     true
                    )
           return
       }else{
           paymentAddress =  paymentAddress.substringAfter(":")
       }

       //lets check query
        if(!queryString.isNullOrEmpty()){


          val queryStringSplit = queryString.split("&")

          for(keyValueStr in queryStringSplit) {

              val splitKeyValue = keyValueStr.split("=")

              if(splitKeyValue.size !=  2){ continue }

              val key = splitKeyValue[0]

              val value = splitKeyValue[1]

              when(key){
                  "amount" -> amount = value.toDoubleOrNull() ?: 0.0
                  //"label"  -> label = value
              }

          }//end for loop


          paymentAddress = paymentAddress.substringBefore("?")

        }//end if

        //fill form with data
        externalAddressToSendInput?.setText(paymentAddress)
        externalAmountToSendInput?.setText(amount.toString())

    }//end fun


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


        //if pincode auth is successful
        if(requestCode == INAPP_AUTH_REQUEST_CODE && resultCode == RESULT_OK){
            sendAssetTransferToServer()
            return
        }//end if


        val barcodeResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data)

        if(barcodeResult != null){

            val barcodeContent = barcodeResult.contents

           if(barcodeContent == null) {
               mActivity?.toast(R.string.operation_aborted_by_user)
               return
           }


            processQRData(barcodeContent)

        } else {

            super.onActivityResult(requestCode, resultCode, data)
        }//end if
    }


    override  fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {

        when(requestCode){

            //if its Bar Code stuff
            APP_CAMERA_PERMISSION -> {

                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    barCodeScanner.initiateScan()
                }else{
                    mActivity!!.toast(R.string.camera_perm_denied)
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {

        @JvmStatic
        fun newInstance(
                layoutId: Int,
               assetInfo: JSONObject
        ) = SendCryptoAssetFragment().apply {

                arguments = Bundle().apply {
                    putInt(LAYOUT_ID_PARAM,layoutId)
                    putString(ASSET_INFO_PARAM, assetInfo.toString())
                }

        } //end apply

    }///end companion



}//end class
