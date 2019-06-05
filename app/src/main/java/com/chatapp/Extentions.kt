package com.chatapp

import android.support.v4.app.Fragment
import android.widget.Toast

fun Fragment.toast(msgRes: Int) {
    Toast.makeText(requireContext(), msgRes, Toast.LENGTH_LONG).show()
}