package com.example.qrcodecompose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.qrcodecompose.data.MainDb
import com.example.qrcodecompose.data.Product
import com.example.qrcodecompose.ui.theme.QRcodeComposeTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var mainDb: MainDb
    var counter = 0

    private val scanLauncher = registerForActivityResult(
        ScanContract()
    ) { result ->
        if (result.contents == null) {
            Toast.makeText(
                this,
                "Scan data is null: ${result.contents}", Toast.LENGTH_SHORT
            ).show()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val productByQr = mainDb.dao.getProductByQr(result.contents)
                if (productByQr == null) {
                    mainDb.dao.insertProduct(
                        Product(
                            null,
                            "Product - ${counter++}",
                            result.contents
                        )
                    )
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Item saved: ${result.contents}", Toast.LENGTH_SHORT
                        ).show()
                    }
                } else{
                    runOnUiThread{
                        Toast.makeText(
                            this@MainActivity,
                            "Dublicated item", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }
    }

    private val scanChecklauncher = registerForActivityResult(
        ScanContract()
    ) { result ->
        if (result.contents == null) {
            Toast.makeText(
                this,
                "Scan data is null: ${result.contents}", Toast.LENGTH_SHORT
            ).show()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val productByQr = mainDb.dao.getProductByQr(result.contents)
                if (productByQr == null) {
                    Toast.makeText(
                        this@MainActivity,
                        "Product not added ${result.contents}", Toast.LENGTH_SHORT
                    ).show()
                } else{
                   mainDb.dao.updateProduct(productByQr.copy(isCheked = true))
                }
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val productStateList = mainDb.dao.getAllProducts()
                .collectAsState(initial = emptyList())

            QRcodeComposeTheme {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f),
                    ) {
                        items(productStateList.value) { product ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 10.dp, end = 10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if(product.isCheked){
                                        Color.Blue
                                    }else{
                                        Color.Yellow
                                    },
                                    contentColor = if(product.isCheked){
                                        Color.Yellow
                                    }else{
                                        Color.Blue
                                    }
                                )
                            ) {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),
                                    text = product.name,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Button(onClick = {
                       scan()
                    }) {
                        Text(text = "Add")
                    }
                    Button(onClick = {
                        scanCheck()
                    }) {
                        Text(text = "Check product")
                    }
                }
            }
        }
    }

    private fun scan() {
        scanLauncher.launch(getScanOptions())
    }

    private fun scanCheck() {
        scanChecklauncher.launch(getScanOptions())
    }

    private fun getScanOptions(): ScanOptions{
        return ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan a barcode")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
        }
    }
}
