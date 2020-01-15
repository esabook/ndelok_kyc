package com.github.esabook.ndelok_kyc;

import androidx.annotation.IntRange;

public class RegionSpec {

    public int WIDTH_CROP_PERCENT;
    public int HEIGHT_CROP_PERCENT;
    public int VERTICAL_OFFSET_PERCENT;
    public int HORIZONTAL_OFFSET_PERCENT;

    public RegionSpec(@IntRange(from = 0, to = 100) int WCrop,
                      @IntRange(from = 0, to = 100) int HCrop,
                      @IntRange(from = -100, to = 100) int VOffset,
                      @IntRange(from = -100, to = 100) int HOffset) {

        this.WIDTH_CROP_PERCENT = WCrop;
        this.HEIGHT_CROP_PERCENT = HCrop;
        this.VERTICAL_OFFSET_PERCENT = VOffset;
        this.HORIZONTAL_OFFSET_PERCENT = HOffset;

    }

}
