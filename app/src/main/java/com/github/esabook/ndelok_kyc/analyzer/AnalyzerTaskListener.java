package com.github.esabook.ndelok_kyc.analyzer;

public interface AnalyzerTaskListener<TResult> {
    void successed(TResult var1);

    void failed(Exception e);

    void completed();
}
