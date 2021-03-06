package com.examen.tais


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.examen.tais.Adapter.Producto
import kotlinx.android.synthetic.main.activity_actualizar.*
import kotlinx.android.synthetic.main.activity_actualizar.Categoria
import kotlinx.android.synthetic.main.activity_actualizar.Categoria_layout
import kotlinx.android.synthetic.main.activity_actualizar.Descripcion
import kotlinx.android.synthetic.main.activity_actualizar.Descripcion_layout
import kotlinx.android.synthetic.main.activity_actualizar.Precio
import kotlinx.android.synthetic.main.activity_actualizar.Precio_layout
import kotlinx.android.synthetic.main.activity_actualizar.Stock
import kotlinx.android.synthetic.main.activity_actualizar.Stock_layout
import kotlinx.android.synthetic.main.activity_actualizar.elegir
import kotlinx.android.synthetic.main.activity_actualizar.enviar
import kotlinx.android.synthetic.main.activity_actualizar.img_categoria
import kotlinx.android.synthetic.main.activity_actualizar.progreso
import kotlinx.android.synthetic.main.activity_registrar.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class Actualizar : AppCompatActivity(), TextWatcher {
    val PICK_IMAGE_REQUEST = 1
    var id:String=""
    private lateinit var bitmap1: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);
        var bundle = intent.extras
        var producto = bundle?.getSerializable("producto") as Producto
        Categoria.setText(producto.categoria)
        Descripcion.setText(producto.producto)
        Precio.setText(producto.precio.toString())
        Stock.setText(producto.stock.toString())

        id = producto.id
        //------------------ Cargamos la imagen del producto desde la url
        Glide
            .with(this).asBitmap()
            .load("https://ventas.ibx.lat/Img/${id}.png").diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    img_categoria.setImageBitmap(resource)
                    bitmap1 = resource
                }
            })

        //------------
        //------------ se implementa un interface, cada vez que se escriba algo desaparesca el error
        Categoria.addTextChangedListener(this)
        Descripcion.addTextChangedListener(this)
        Precio.addTextChangedListener(this)
        Stock.addTextChangedListener(this)
        //------------------------------------------


        //elegimos la imagen en nuestra unidad
        elegir.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Selecciona una imagen"),
                PICK_IMAGE_REQUEST
            )
        }
        //Enviar la solicitud para actualizar datos
        enviar.setOnClickListener{

            // Verificar que los datos no esten vacios si no saldran error para cada Atributo
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
            else {


                progreso.visibility = View.VISIBLE//mostramos el progressbar
                val stringRequest = object : StringRequest(
                    Method.POST,
                    "https://ventas.ibx.lat/updateproducto.php",
                    Response.Listener { response ->


                    },
                    Response.ErrorListener {

                        Log.i("informacion", it.toString())
                    }) {
                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()

                        //le pasamos los Id y datos para enviar con la solicitud
                        params.put("Categoria", Categoria.text.toString())
                        params.put("Descripcion", Descripcion.text.toString())
                        params.put("Precio", Precio.text.toString())
                        params.put("Stock", Stock.text.toString())
                        params.put("Id", id)
                        return params
                    }

                }
                val string1Request = object : StringRequest(
                    Method.POST,
                    "https://ventas.ibx.lat/updateproducto_img.php",
                    Response.Listener { response ->
                        Toast.makeText(this, response.toString(), Toast.LENGTH_SHORT).show()
                    },
                    Response.ErrorListener {

                    }) {
                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()

                        val valor = imageToString(bitmap1)
                        params.put("img", valor)
                        params.put("Id", id)
                        return params
                    }

                }
                val queQue = Volley.newRequestQueue(this)
                queQue.add(stringRequest)
                queQue.addRequestFinishedListener(object :
                    RequestQueue.RequestFinishedListener<String> {
                    override fun onRequestFinished(request: Request<String?>?) {
                        val que = Volley.newRequestQueue(baseContext)
                        string1Request.retryPolicy =
                            DefaultRetryPolicy(
                                30000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                            )


                        que.add(string1Request)
                        //Cuando acabe la solicitud desaparece el progressbar
                        que.addRequestFinishedListener(object :
                            RequestQueue.RequestFinishedListener<String> {
                            override fun onRequestFinished(request: Request<String?>?) {
                                progreso.visibility = View.GONE
                            }
                        })
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
                bitmap1 = MediaStore.Images.Media.getBitmap(
                    getContentResolver(),
                    filePath
                )
                img_categoria.setImageBitmap(bitmap1)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Convierte una bitmap en un string,esto permitira enviar un foto como un string  en Base64
    fun imageToString(bitmap: Bitmap):String{
        val outputStream= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
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