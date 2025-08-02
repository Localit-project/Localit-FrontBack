package com.inhatc.localit.ui.category;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CategoryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CategoryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("카테고리");
    }

    public LiveData<String> getText() {
        return mText;
    }
}