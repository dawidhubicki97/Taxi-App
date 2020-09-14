package com.example.pracainz.adapters

import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pracainz.R
import com.example.pracainz.models.AvailableDrive
import com.example.pracainz.models.OrdersInProgress
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.LocationCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.list_item.view.*

class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    private var items: List<AvailableDrive> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DriveViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        )
    }
    fun submitList(blogList: List<AvailableDrive>){
        items = blogList
    }
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {

            is DriveViewHolder -> {
                holder.bind(items.get(position))
            }
        }
    }

}

class DriveViewHolder
constructor(itemView: View): RecyclerView.ViewHolder(itemView){

    val title = itemView.list_title
    val description = itemView.list_description
    fun bind(drive: AvailableDrive){
        title.setText(drive.user)
        description.setText(drive.lat.toString()+" "+drive.lng.toString())
        itemView.setOnClickListener {
            var firstlocation:GeoLocation?=null
            var secondlocation:GeoLocation
            val uid= FirebaseAuth.getInstance().uid
           var ref= FirebaseDatabase.getInstance().getReference("/OrderRequests")
           var geoFire = GeoFire(ref)
            geoFire.getLocation(drive.user,object:LocationCallback{
                override fun onLocationResult(key: String?, location: GeoLocation?) {
                    if(location!=null) {
                        firstlocation = location
                        geoFire.removeLocation(drive.user,GeoFire.CompletionListener { key, error ->

                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError?) {

                }

            })

            var refsecond= FirebaseDatabase.getInstance().getReference("/OrderRequestsTarget")
            var geoFiresecond = GeoFire(refsecond)
            geoFiresecond.getLocation(drive.user,object:LocationCallback{
                override fun onLocationResult(key: String?, location: GeoLocation?) {
                    if(location!=null) {
                        geoFiresecond.removeLocation(drive.user,GeoFire.CompletionListener { key, error ->
                            secondlocation = location
                            refsecond = FirebaseDatabase.getInstance().getReference("/OrdersInProgress").push()
                            var orderinprogress = OrdersInProgress(uid!!, key!!, firstlocation!!.latitude,firstlocation!!.longitude, secondlocation!!.latitude,secondlocation!!.longitude)
                            refsecond.setValue(orderinprogress)
                        })


                    }
                }

                override fun onCancelled(databaseError: DatabaseError?) {

                }

            })


        }
    }

}