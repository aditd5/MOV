package com.aditd5.mov.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

@Suppress("DEPRECATION")
data class Movie(
    var movieId: String? = null ,
    var title: String? = null ,
    var genres: String? = null ,
    var runtime: Int? = null ,
    var rating: String? = null ,
    var synopsis: String? = null ,
    var posterUrl: String? = null ,
    var releaseDate: Timestamp? = null ,
    var lastScreeningDate: Timestamp? = null ,
    var price: Int? = null ,
    var cast: List<Map<String, String>> = listOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readValue(Int::class.java.classLoader) as? Int ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readParcelable(Timestamp::class.java.classLoader) ,
        parcel.readParcelable(Timestamp::class.java.classLoader) ,
        parcel.readValue(Int::class.java.classLoader) as? Int ,
        mutableListOf<HashMap<String, String>>().apply {
            parcel.readList(this as List<*>, HashMap::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel , flags: Int) {
        parcel.writeString(movieId)
        parcel.writeString(title)
        parcel.writeString(genres)
        parcel.writeValue(runtime)
        parcel.writeString(rating)
        parcel.writeString(synopsis)
        parcel.writeString(posterUrl)
        parcel.writeParcelable(releaseDate , flags)
        parcel.writeParcelable(lastScreeningDate , flags)
        parcel.writeValue(price)
        parcel.writeList(cast.map { HashMap(it) })
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


