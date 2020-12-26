package com.examen.tais.Adapter

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.examen.tais.Actualizar
import com.examen.tais.R
import de.hdodenhof.circleimageview.CircleImageView
import java.io.Serializable

// Clase producto
data class Producto(
    val id: String,
    val producto: String,
    val categoria: String,
    val precio: Float,
    val stock: Int
):Serializable

//----------------------
class AdapterProducto: RecyclerView.Adapter<AdapterProducto.MyviewHolder>(), Filterable {
    private lateinit var context: Context
        var datos:MutableList<Producto> =  ArrayList()
        var datos_all:MutableList<Producto> = ArrayList()
    fun data(data: MutableList<Producto>, context: Context)
    {
        datos=data
        this.context=context
        datos_all=data
    }


    class MyviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_producto=itemView.findViewById(R.id.txt_producto) as TextView
        var txt_categoria=itemView.findViewById(R.id.txt_categoria) as TextView
        var txt_precio=itemView.findViewById(R.id.txt_precio) as TextView
        var txt_stock=itemView.findViewById(R.id.txt_stock) as TextView
        var img_producto=itemView.findViewById(R.id.img_producto) as CircleImageView
        var contenedor=itemView.findViewById(R.id.Contenedor) as LinearLayout

        fun bind(producto: Producto, context: Context) {
            txt_producto.text = producto.producto

            txt_categoria.text = producto.categoria
            txt_precio.text ="Precio:"+ producto.precio.toString()
            txt_stock.text ="Stock:"+ producto.stock.toString()


            contenedor.setOnClickListener{
                var intent= Intent(context, Actualizar::class.java)
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                var bundle=Bundle()
                bundle.putSerializable("producto", producto)
                intent.putExtras(bundle)
                context.startActivity(intent)
            }
        //----------Uso de la libreria Glide  para visualizar la imagen en el CircleImagenView desde una Url y tambien las opciones de no guardar en la cache
         Glide.with(context).load("https://ventas.ibx.lat/Img/${producto.id}.png").diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true).into(img_producto)
          //  Glide.with(context).load("https://ventas.ibx.lat/Img/${producto.id}.png").into(img_producto)
        //-----------------------------------------
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyviewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.row_producto, parent, false)

        val myviewHolder=MyviewHolder(view)

        return myviewHolder
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
       val producto= datos.get(position)
        holder.bind(producto, context)

    }
    fun limpiar()
    {
        datos.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount() =datos.size
            // se implementa el filtro de busquedad personalisado , donde buscara por la descripcion del producto
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                if (constraint.toString().isEmpty()) {
                    datos = datos_all
                } else {
                    val filteredList: MutableList<Producto> = java.util.ArrayList<Producto>()
                    for (producto in datos_all) {
                        if (producto.producto.toLowerCase()
                                .contains(constraint.toString().toLowerCase())
                        ) {
                            filteredList.add(producto)
                        }
                    }
                    datos = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values =datos

                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                datos = results?.values as ArrayList<Producto>
                notifyDataSetChanged()
            }

        }
    }
    //------------------------------------------

}