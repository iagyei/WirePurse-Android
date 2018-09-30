package com.transcodium.tnsmoney

import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_header.*

class ReceiveCryptoAssetActivity : ActivityDialogBase() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_crypto_asset)

        closeModal.setOnClickListener {this.finish()}
    }
}
