package com.syed.faseeh.darktoken

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Adapter
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.syed.faseeh.darktoken.Adapter.CoinAdapter
import com.syed.faseeh.darktoken.Common.Common
import com.syed.faseeh.darktoken.Interface.ILoadMore
import com.syed.faseeh.darktoken.Model.CoinModel
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity(), ILoadMore {

    internal var items:MutableList<CoinModel> = ArrayList()
    internal  lateinit var adapter:CoinAdapter
    internal lateinit var client:OkHttpClient
    internal lateinit var request: Request

    override fun onLoadMore() {
        if(items.size<= Common.MAX_COIN_LOAD)
            loadNext10coin(items.size)
        else
            Toast.makeText(this@MainActivity,"Data Max is "+Common.MAX_COIN_LOAD, Toast.LENGTH_SHORT).show()
    }

    private fun  loadNext10coin(index: Int){
        client= OkHttpClient()
        request= Request.Builder().url(String.format("https://api.coinmarketcap.com/v1/ticker/?convert=INR&start=%d&limit=10",index)).build()

        swipe_to_refresh.isRefreshing=true
        client.newCall(request).enqueue(object :Callback
        {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("Error",e.toString())
            }

            override fun onResponse(call: Call?, response: Response) {
                val body= response.body()!!.string()
                val gson=Gson()
                val newItems =gson.fromJson<List<CoinModel>>(body,object:TypeToken<List<CoinModel>>(){}.type)
                runOnUiThread{
                        items.addAll(newItems)
                    adapter.setLoaded()
                    adapter.updateData(items)

                    swipe_to_refresh.isRefreshing=false;

                }
            }
        })



    }

    private fun  loadFirst10coin(){
        client= OkHttpClient()
        request= Request.Builder().url(String.format("https://api.coinmarketcap.com/v1/ticker/?convert=INR&limit=10")).build()

        client.newCall(request).enqueue(object :Callback
        {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("Error",e.toString())
            }

            override fun onResponse(call: Call?, response: Response) {
                val body= response.body()!!.string()
                val gson=Gson()
                items =gson.fromJson(body,object:TypeToken<List<CoinModel>>(){}.type)
                runOnUiThread{
                    adapter.updateData(items)


                }
            }
        })



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipe_to_refresh.post{ loadFirst10coin()}

        swipe_to_refresh.setOnRefreshListener {
            items.clear()
            loadFirst10coin()
            setupAdapter()
        }
        coin_recycler_view.layoutManager=LinearLayoutManager(this)
        setupAdapter()
    }


    private fun setupAdapter(){
        adapter= CoinAdapter(coin_recycler_view,this@MainActivity,items)
        coin_recycler_view.adapter=adapter
        adapter.setLoadMore(this)
    }
}
