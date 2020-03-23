package be.florien.anyflow.feature

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.florien.anyflow.extension.eLog
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

open class BaseViewModel : ViewModel() {

    /**
     * RxJava
     */
    private val disposableContainerMap = mutableMapOf<String, CompositeDisposable>()

    open fun destroy() {
        for (container in disposableContainerMap.values) {
            container.clear()
        }
    }

    fun <T> subscribe(observable: Observable<T>, onNext: ((T) -> Unit), onError: ((Throwable) -> Unit) = this::logOnError, onComplete: (() -> Unit) = this::doNothingOnComplete, containerKey: String = "Default") {
        getContainer(containerKey).add(observable.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError, onComplete))
    }

    fun subscribe(completable: Completable, onError: (Throwable) -> Unit = this::logOnError, onComplete: () -> Unit = this::doNothingOnComplete, containerKey: String = "Default") {
        getContainer(containerKey).add(completable.observeOn(AndroidSchedulers.mainThread()).subscribe(onComplete, onError))
    }

    fun <T> subscribe(flowable: Flowable<T>, onNext: ((T) -> Unit), onError: ((Throwable) -> Unit) = this::logOnError, onComplete: (() -> Unit) = this::doNothingOnComplete, containerKey: String = "Default") {
        getContainer(containerKey).add(flowable.observeOn(AndroidSchedulers.mainThread()).doOnNext(onNext).doOnError(onError).doOnComplete(onComplete).subscribe())
    }

    fun dispose(containerKey: String) {
        disposableContainerMap[containerKey]?.clear()
    }

    private fun getContainer(containerKey: String): CompositeDisposable {
        val container = disposableContainerMap[containerKey]
        return if (container == null) {
            val newContainer = CompositeDisposable()
            disposableContainerMap[containerKey] = newContainer
            newContainer
        } else {
            container
        }
    }

    private fun doNothingOnComplete() {
    }

    private fun logOnError(throwable: Throwable) {
        eLog(throwable, "Standard error message from a ViewModel")
    }

    /**
     * LiveData
     */

    protected val <T> LiveData<T>.mutable: MutableLiveData<T>
            get() = this as MutableLiveData<T>

    protected val <T> ValueLiveData<T>.mutable: MutableValueLiveData<T>
            get() = this as MutableValueLiveData<T>
}