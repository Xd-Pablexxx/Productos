package com.examen.tais

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.examen.tais.Adapter.AdapterProducto
import com.examen.tais.Adapter.Producto
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    var productos: MutableList<Producto> = ArrayList()
    val adaptador = AdapterProducto()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_main)
        btn_registrar.setOnClickListener {
            val intent = Intent(this, Registrar::class.java)
            startActivity(intent)
        }


        RV_Productos.setHasFixedSize(true)

        RV_Productos.layoutManager = LinearLayoutManager(this)


        //--------- para poder deslizar el item desde la derecha y poder eliminarlo
        var item = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false;
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                eliminar(productos.get(viewHolder.adapterPosition).id)
                productos.removeAt(viewHolder.adapterPosition)
                adaptador.notifyDataSetChanged()
            }
        }

        ItemTouchHelper(item).attachToRecyclerView(RV_Productos)
        //-----------------------------------------------------------------------------
    }
    //----------- Funcion llamada despues de deslizar de derecha a izquierda
    fun eliminar(id:String)
    {
        val stringRequest=object : StringRequest(
            Method.POST,"https://ventas.ibx.lat/deleteproducto.php", Response.Listener {
                    response ->
                   Toast.makeText(baseContext,"Eliminado",Toast.LENGTH_SHORT).show()

            },
            Response.ErrorListener {

                Log.i("informacion",it.toString())
            })
        {
            override fun getParams(): MutableMap<String, String> {
                val params= HashMap<String,String>()
                params.put("Id",id)
                return params
            }

        }
        val queQue= Volley.newRequestQueue(this)
        queQue.add(stringRequest)
    }
    //---------------------------------------

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main,menu)

        return true

    }
    //---------Para el buscador en la parte superior y poder hacer la busqueda
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.buscar->
            {
                //---------------Implementacion del evento SetOnQueryTextListener para poder saber que escribio en el buscador
                val searchView = item.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        //Usar el filtro incorporado en el adaptador
                        adaptador.filter.filter(newText)
                        return false
                    }
                })
                true
            }
        }
        return true
    }
    //------------------------------------------------------
    override fun onStart() {
        super.onStart()
        adaptador.limpiar()
        data()

    }
//funcion para solicitar los productos mediante la libreria Volley
    fun data()
    {

        var request = JsonArrayRequest("https://ventas.ibx.lat/productos.php",
            Response.Listener<JSONArray> { response ->


                for (i in 0..response.length() - 1) {
                    val jsonObject = response.getJSONObject(i)
                    val producto = Producto(
                        jsonObject.getString("Id"),
                        jsonObject.getString("Descripcion"),
                        jsonObject.getString("Categoria"),
                        jsonObject.getString("Precio").toFloat(),
                        jsonObject.getString("Stock").toInt()
                    )

                    productos.add(producto)
                }
                adapter(productos)

            },
            Response.ErrorListener
            {
                Log.i("error1", it.toString())
            })
        val queQue= Volley.newRequestQueue(this)
        queQue.add(request)
       

    }
//-----------------------------------------------

    //--------------Pasamos los datos obtenidos al adaptador ----------------
    fun adapter(productos: MutableList<Producto>)
    {

        adaptador.data(productos, baseContext)
        RV_Productos.adapter=adaptador
    }
    //-----------------------------------------------
}