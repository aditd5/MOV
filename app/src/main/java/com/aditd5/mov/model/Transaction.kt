package com.aditd5.mov.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

@Suppress("DEPRECATION")
data class Transaction(
    var transactionId: String? = null ,
    var orderId: String? = null ,
    var movieId: String? = null ,
    var showTime: Timestamp? = null ,
    var price: Int? = null ,
    var seats: List<String>? = listOf() ,
    var passcode: String? = null ,
    var paymentMethod: String? = null ,
    var createdAt: Timestamp? = null ,
    var movie: Movie? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readParcelable(Timestamp::class.java.classLoader) ,
        parcel.readValue(Int::class.java.classLoader) as? Int ,
        parcel.createStringArrayList() ,
        parcel.readString() ,
        parcel.readString() ,
        parcel.readParcelable(Timestamp::class.java.classLoader) ,
        parcel.readParcelable(Movie::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel , flags: Int) {
        parcel.writeString(transactionId)
        parcel.writeString(orderId)
        parcel.writeString(movieId)
        parcel.writeParcelable(showTime , flags)
        parcel.writeValue(price)
        parcel.writeStringList(seats)
        parcel.writeString(passcode)
        parcel.writeString(paymentMethod)
        parcel.writeParcelable(createdAt , flags)
        parcel.writeParcelable(movie , flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }
}