package com.transcodium.tnsmoney


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.transcodium.tnsmoney.classes.AppAlert
import com.transcodium.tnsmoney.classes.WalletCore
import kotlinx.android.synthetic.main.send_crypto_asset_external.*
import kotlinx.android.synthetic.main.send_crypto_asset_external.view.*
import org.jetbrains.anko.toast
import java.net.URL


private const val LAYOUT_ID_PARAM = "layout_id"
private const val ASSET_SYMBOL_PARAM = "asset_symbol"
private const val ASSET_ID_PARAM = "asset_id"
private const val ASSET_CHAIN_PARAM = "asset_chain"


class SendCryptoAssetFragment : Fragment() {


    private var assetSymbol: String? = null
    private var assetId: String? = null
    private var assetChain: String? = null
    private var layoutId: Int? = null
    private  val APP_CAMERA_PERMISSION = 15
    //private var rootView: View? = null


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
            assetSymbol = it.getString(ASSET_SYMBOL_PARAM)
            assetId = it.getString(ASSET_ID_PARAM)
            assetChain = it.getString(ASSET_CHAIN_PARAM)
            layoutId = it.getInt(LAYOUT_ID_PARAM)
        }

    }

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

        rootView.addressToSendInputLayout?.hint = getString(
                R.string.asset_name_space_address,chainNameAndSymbol
        )

        return rootView
    }

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
        addressToSendInput?.setText(paymentAddress)
        amountToSendInput?.setText(amount.toString())

    }//end fun


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

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
