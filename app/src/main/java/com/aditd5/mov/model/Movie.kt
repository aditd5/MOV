package com.aditd5.mov.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Movie(
    var title: String? = null ,
    var synopsis: String? = null ,
    var genre: String? = null ,
    var posterUrl: String? = null ,
    var rating: String? = null ,
    var price: Int? = null ,
    var releaseDate: Timestamp? = null ,
    var actors: List<Map<String, String>> = listOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Timestamp::class.java.classLoader)  as? Timestamp,
        mutableListOf<HashMap<String, String>>().apply {
            parcel.readList(this as List<*>, HashMap::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel , flags: Int) {
        parcel.writeString(title)
        parcel.writeString(synopsis)
        parcel.writeString(genre)
        parcel.writeString(posterUrl)
        parcel.writeString(rating)
        parcel.writeValue(price)
        parcel.writeValue(releaseDate)
        parcel.writeList(actors.map { HashMap(it) })
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Movie> {
        override fun createFromParcel(parcel: Parcel): Movie {
            return Movie(parcel)
        }

        override fun newArray(size: Int): Array<Movie?> {
            return arrayOfNulls(size)
        }
    }
}

//data class Actor (
//    val name: String? = null,
//    val photo: String? = null
//) : Parcelable {
//    constructor(parcel: Parcel) : this(
//        parcel.readString() ,
//        parcel.readString()
//    )
//
//    override fun writeToParcel(parcel: Parcel , flags: Int) {
//        parcel.writeString(name)
//        parcel.writeString(photo)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<Actor> {
//        override fun createFromParcel(parcel: Parcel): Actor {
//            return Actor(parcel)
//        }
//
//        override fun newArray(size: Int): Array<Actor?> {
//            return arrayOfNulls(size)
//        }
//    }
//}