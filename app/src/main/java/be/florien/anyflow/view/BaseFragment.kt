package be.florien.anyflow.view

import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {
    abstract fun getTitle(): String
}