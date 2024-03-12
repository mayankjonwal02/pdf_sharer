package com.example.spandan_file_sender.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.spandan_file_sender.android.API.APIViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("CoroutineCreationDuringComposition", "CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(Intent.ACTION_SEND == intent.action)
        {
            setContent {
//                MyApplicationTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {

                        val pdfURI:Uri? = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        pdfURI?.let {

                            var info = remember {
                                mutableStateOf("Sending File....")
                            }
                            val context = LocalContext.current
                            val myviewmodel = APIViewModel(context)


                            Text(text = info.value)
                            getpermissiononsend(context,myviewmodel,pdfURI,info)
//                            LaunchedEffect(info)
//                            {
//                                Toast.makeText(context,info.value,Toast.LENGTH_SHORT).show()
//                            }



                        }
//                    }
                }
            }



        }
        else
        {


            setContent {
                var context = LocalContext.current
                var ipaddress by remember {
                    mutableStateOf(getsharedpref(context)?.getString("ipaddress","0.0.0.0"))
                }
//                MyApplicationTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val context = LocalContext.current
//                        val myviewmodel = APIViewModel(context)
                        var info by remember {
                            mutableStateOf("fetching data")
                        }
//                        CoroutineScope(Dispatchers.IO).launch {
//                            info = myviewmodel.mydata()
//                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color(android.graphics.Color.parseColor("#F5F5DC")))
                        )
                        {


                           Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                               Text(text = info)
                               TextField(value = ipaddress!!, onValueChange = {ipaddress = it
                               getsharedpref(context)?.edit()?.putString("ipaddress",it)?.apply()
                               })
//                               TextField(value = "hello", onValueChange ={} )


                           }

                        }

                        getpermission(context = context)
                    }
                }
//            }
        }
    }

}


@OptIn(DelicateCoroutinesApi::class)
fun SaveSendFile(context: Context, uri: Uri, myviewmodel: APIViewModel, info: MutableState<String>)
{
    var maindirname = "SpandanPdfFiles"
    var maindir = File(context.getExternalFilesDir(null),maindirname)
    var sourceinputstream = context.contentResolver.openInputStream(uri)
    if(!maindir.exists())
    {
        maindir.mkdir()
    }
    var pdffile = File(maindir,File(uri.path).name)
    if(pdffile.exists())
    {
        pdffile.delete()
    }

    GlobalScope.launch(Dispatchers.Main)
    {
        withContext(Dispatchers.Main)
        {
            sourceinputstream?.use {
                input ->
                FileOutputStream(pdffile).use {
                    output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(context,pdffile.name + " saved",Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.IO).launch {
                if(pdffile.exists()){
                    info .value =
                        myviewmodel.uploadPdf(context, pdffile.absolutePath)
                    pdffile.delete()
                }
            }

        }

    }
}



@Composable
fun getpermission(context: Context)
{
    val permissionlauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(), onResult =
    {
        permissions ->
        val allpermissiongranted = permissions.all { it.value }
        if (allpermissiongranted)
        {
            Toast.makeText(context,"No File Choosen",Toast.LENGTH_SHORT).show()
        }
        else
        {
            val notGrantedPermissions = permissions.filter { !it.value }.keys.joinToString(", ")
            Toast.makeText(context, "Permissions not granted: $notGrantedPermissions", Toast.LENGTH_SHORT).show()
        }
    })
    LaunchedEffect(Unit)
    {
//        permissionlauncher.launch(
//
//            arrayOf(
////                Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//
//        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT > 31) {
                // For Android 13 (S) and above
                Toast.makeText(context,"ANDROID >= 13 & ${Build.VERSION.SDK_INT} & ${Build.VERSION.RELEASE}",Toast.LENGTH_SHORT).show()
                permissionlauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            } else {
                // For Android 12 (S) and below
                Toast.makeText(context,"ANDROID <= 12 & ${Build.VERSION.SDK_INT} & ${Build.VERSION.RELEASE}",Toast.LENGTH_SHORT).show()
                permissionlauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        } else {
            Toast.makeText(context,"Lower Version ${Build.VERSION.SDK_INT} & ${Build.VERSION.RELEASE}",Toast.LENGTH_SHORT).show()

        }


    }
}


@Composable
fun getpermissiononsend(
    context: Context,
    myviewmodel: APIViewModel,
    pdfURI: Uri,
    info: MutableState<String>
)
{
    val permissionlauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(), onResult =
    {
            permissions ->
        val allpermissiongranted = permissions.all { it.value }
        if (allpermissiongranted)
        {
            SaveSendFile(context,pdfURI,myviewmodel,info)
        }
        else
        {
            val notGrantedPermissions = permissions.filter { !it.value }.keys.joinToString(", ")
            Toast.makeText(context, "Permissions not granted: $notGrantedPermissions", Toast.LENGTH_SHORT).show()
        }
    })
    LaunchedEffect(Unit)
    {

//        permissionlauncher.launch(
//
//            arrayOf(
////                Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//
//        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT > 31) {
                // For Android 13 (S) and above
                Toast.makeText(context,"ANDROID >= 13 & ${Build.VERSION.SDK_INT} & ${Build.VERSION.RELEASE}",Toast.LENGTH_SHORT).show()
                permissionlauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            } else {
                // For Android 12 (S) and below
                Toast.makeText(context,"ANDROID <= 12 & ${Build.VERSION.SDK_INT} & ${Build.VERSION.RELEASE}",Toast.LENGTH_SHORT).show()
                permissionlauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        } else {
            SaveSendFile(context, pdfURI, myviewmodel, info)

        }



    }
}


fun getsharedpref(context: Context): SharedPreferences? {
    return context.getSharedPreferences("myprefs",Context.MODE_PRIVATE)
}





