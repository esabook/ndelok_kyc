package com.github.esabook.idcardsubmission.analyzer;

public interface AnalyzerTaskListener<TResult> {
    void successed(TResult var1);

    void failed(Exception e);

    void completed();
}
