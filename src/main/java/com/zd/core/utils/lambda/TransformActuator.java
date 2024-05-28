package com.zd.core.utils.lambda;

public interface TransformActuator<I, O> {

    O execute(I param);
}
