package com.examen.tais

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_actualizar.*
import kotlinx.android.synthetic.main.activity_registrar.*
import kotlinx.android.synthetic.main.activity_registrar.Categoria
import kotlinx.android.synthetic.main.activity_registrar.Descripcion
import kotlinx.android.synthetic.main.activity_registrar.Precio
import kotlinx.android.synthetic.main.activity_registrar.Stock
import kotlinx.android.synthetic.main.activity_registrar.elegir
import kotlinx.android.synthetic.main.activity_registrar.enviar
import kotlinx.android.synthetic.main.activity_registrar.img_categoria
import kotlinx.android.synthetic.main.activity_registrar.progreso
import kotlinx.android.synthetic.main.activity_registrar.Categoria_layout
import kotlinx.android.synthetic.main.activity_registrar.Descripcion_layout
import kotlinx.android.synthetic.main.activity_registrar.Precio_layout
import kotlinx.android.synthetic.main.activity_registrar.Stock_layout


import java.io.ByteArrayOutputStream
import java.io.IOException

class Registrar : AppCompatActivity(), TextWatcher {
    val PICK_IMAGE_REQUEST = 1
    var id:String=""
    private lateinit var bitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);
        elegir.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Selecciona una imagen"),
                PICK_IMAGE_REQUEST
            )
        }
        Categoria.addTextChangedListener(this)
        Descripcion.addTextChangedListener(this)
        Precio.addTextChangedListener(this)
        Stock.addTextChangedListener(this)
        enviar.setOnClickListener{

            if(Categoria.text.toString()=="")
            {
                Categoria_layout.error="Error en Categoria"
            }
            else if (Descripcion.text.toString()=="")
            {
                Descripcion_layout.error="Error en Descripcion"
            }
            else if (Precio.text.toString()=="")
            {
                Precio_layout.error="Error en Precio"
            }
            else if (Stock.text.toString()=="")
            {
                Stock_layout.error="Error en Stock"
            }
            else
            {
                progreso.visibility=View.VISIBLE
                val stringRequest=object : StringRequest(
                    Method.POST,"http://ventas.ibx.lat/createproducto.php", Response.Listener {
                            response ->
                        id=response.toString()

                    },
                    Response.ErrorListener {

                        Log.i("informacion",it.toString())
                    })
                {
                    override fun getParams(): MutableMap<String, String> {
                        val params= HashMap<String,String>()

                        //params.put("img",valor)
                        params.put("Categoria",Categoria.text.toString())
                        params.put("Descripcion",Descripcion.text.toString())
                        params.put("Precio",Precio.text.toString())
                        params.put("Stock",Stock.text.toString())
                        return params
                    }

                }
                val string1Request=object : StringRequest(
                    Method.POST,"http://ventas.ibx.lat/createproducto_img.php", Response.Listener {
                            response ->
                        Toast.makeText(this,response.toString(), Toast.LENGTH_SHORT).show()
                    },
                    Response.ErrorListener {
                        Toast.makeText(this,it.toString(), Toast.LENGTH_SHORT).show()
                    })
                {
                    override fun getParams(): MutableMap<String, String> {
                        val params= HashMap<String,String>()
                        val valor=imageToString(bitmap)
                        params.put("img",valor)
                        params.put("Id",id)
                        return params
                    }

                }
                val queQue= Volley.newRequestQueue(this)
                queQue.add(stringRequest)
                val que=Volley.newRequestQueue(this)
                string1Request.retryPolicy =
                    DefaultRetryPolicy(
                        30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
                que.add(string1Request)
                que.addRequestFinishedListener(object:RequestQueue.RequestFinishedListener<String> {
                    override fun onRequestFinished(request: Request<String?>?) {
                        progreso.visibility=View.GONE
                    }
                })
            }




            //queQue.add(string1Request)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val filePath = data.data
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(),
                    filePath
                )
                img_categoria.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    fun imageToString(bitmap: Bitmap):String{
        val outputStream= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        val imageByte=outputStream.toByteArray()
        val encodeImageView= Base64.encodeToString(imageByte, Base64.DEFAULT)
        return encodeImageView
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            Categoria_layout.error=null
            Descripcion_layout.error=null
            Precio_layout.error=null
            Stock_layout.error=null
    }

    override fun afterTextChanged(p0: Editable?) {

    }
}