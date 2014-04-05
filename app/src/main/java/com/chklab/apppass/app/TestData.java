/*
 * Copyright (c) 2013-2014 Aplix and/or its affiliates. All rights reserved.
 */
package com.chklab.apppass.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import com.aplixcorp.android.ble.beacon.BeaconRegion;

/**
 * ビーコン領域情報テーブル.
 *
 * お手持ちのビーコン情報に置き換えてご使用ください.
 */
public class TestData {

    private TestData() {
    }
    /** proximityUUID - known uuid */ 
//[STEP.1] fromString() の引数をお手持ちの ビーコンの UUID に変更してください.
    private static final UUID sBEACON_UUID_0 = UUID.fromString("00000000-FB6E-1001-B000-001C4DF7B362");

//[STEP.2] UUID が異なる複数のビーコンをお使いの場合は、 sBEACON_UUID_x を追加してください.
//    /** proximityUUID - known uuid */ 
//    private static final UUID sBEACON_UUID_1 = UUID.fromString("11111111-2222-3333-4444-555555555555");
//    .....

    /**
     * テスト用の領域定義.
     * < String:領域名, BeaconRange:ビーコン領域 >
     */
    @SuppressWarnings("serial")
    private static final HashMap<String,BeaconRegion> mRegionMap = new HashMap<String,BeaconRegion>() {
        {
//[STEP.3] お手持ちのビーコンの major, minor 値に応じて、適宜領域を追加してください.
//        (name は任意の文字列です)

            String name;
            //UUID共通(major 無指定)の領域
            name = "ALL";       put(name, new BeaconRegion(sBEACON_UUID_0, BeaconRegion.DEFAULT, BeaconRegion.DEFAULT, name));

            //UUID,major共通(minor 無指定)の領域
            name = "MAJOR0000"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0000, BeaconRegion.DEFAULT, name));
            name = "MAJOR0001"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0001, BeaconRegion.DEFAULT, name));

            //ビーコン個別の領域.
            name = "REGION-00"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0000, 0x0000, name));
            name = "REGION-01"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0000, 0x0001, name));
//            name = "REGION-02"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0000, 0x0002, name));
//            name = "REGION-03"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0000, 0x0003, name));
//            name = "REGION-04"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0000, 0x0004, name));
//            ....
//            name = "REGION-10"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0001, 0x0000, name));
//            name = "REGION-11"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0001, 0x0001, name));
//            name = "REGION-12"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0001, 0x0002, name));
//            name = "REGION-13"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0001, 0x0003, name));
            name = "REGION-14"; put(name, new BeaconRegion(sBEACON_UUID_0, 0x0001, 0x0004, name));
//            ....

//            name = "JM1_xxxx"; put(name, new BeaconRegion(sBEACON_UUID_1, 0x0000, 0x0000, name));
//            ....
        }
    };
    @SuppressWarnings("serial")
    private static final ArrayList<String> sRegionNames = new ArrayList<String>(mRegionMap.keySet()) {
        {
            Collections.sort(this);
        }
    };
    @SuppressWarnings("serial")
    private static final ArrayList<BeaconRegion> sRegions = new ArrayList<BeaconRegion>() {
        {
            for(String s: sRegionNames) {
                add(mRegionMap.get(s));
            }
        }
    };

    static ArrayList<String> getRegionNames() {
        return new ArrayList<String>(sRegionNames);
    }

    static ArrayList<BeaconRegion> getRegions() {
        return new ArrayList<BeaconRegion>(sRegions);
    }

    static BeaconRegion getRegion(String regionName) {
        return mRegionMap.get(regionName);
    }

}
