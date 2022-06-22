package com.xiangxue.network.observer;

import com.arch.demo.core.model.MvvmBaseModel;
import com.arch.demo.core.model.MvvmDataObserver;
import com.xiangxue.network.errorhandler.ExceptionHandle;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

 
public class BaseObserver<T> implements Observer<T> {
    MvvmBaseModel baseModel;
    MvvmDataObserver<T> mvvmDataObserver;
    public BaseObserver(MvvmBaseModel baseModel, MvvmDataObserver<T> mvvmDataObserver) {
        this.baseModel = baseModel;
        this.mvvmDataObserver = mvvmDataObserver;
    }
    @Override
    public void onError(Throwable e) {
        if(e instanceof ExceptionHandle.ResponeThrowable){
            mvvmDataObserver.onFailure(e);
        } else {
            mvvmDataObserver.onFailure(new ExceptionHandle.ResponeThrowable(e, ExceptionHandle.ERROR.UNKNOWN));
        }
    }

    @Override
    public void onNext(T t) {
        mvvmDataObserver.onSuccess(t, false);
    }

    @Override
    public void onSubscribe(Disposable d) {
        if(baseModel != null){
            baseModel.addDisposable(d);
        }
    }

    @Override
    public void onComplete() {
    }
}
