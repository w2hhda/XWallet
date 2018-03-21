package net.bither.bitherj.core;

/**
 * Created by wuliang on 18-3-15.
 */

public abstract class AbstractHD {
    public enum PathType {
        EXTERNAL_ROOT_PATH(0), INTERNAL_ROOT_PATH(1);
        private int value;

        PathType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }


    }
}
