package com.yws.faceid.wcl;

import com.estone.zhouxinliang.eid_id_sam_reader.LUtils;
import com.zkteco.android.IDReader.WLTService;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by wangcl on 2019/3/21.
 */

public class IDCardInfo {
    private  String TAG="IDCardInfo";
    private byte[] photo;

    private static final String[] NATION_ARRAY =  new String[]{"汉", "蒙古", "回", "藏", "维吾尔", "苗", "彝", "壮", "布依", "朝鲜",
            "满", "侗", "瑶", "白", "土家", "哈尼", "哈萨克", "傣", "黎", "傈僳",
            "佤", "畲", "高山", "拉祜", "水", "东乡", "纳西", "景颇", "克尔克孜", "土",
            "达斡尔", "仫佬", "羌", "布朗", "撒拉", "毛南", "仡佬", "锡伯", "阿昌", "普米",
            "塔吉克", "怒", "乌兹别克", "俄罗斯", "鄂温克", "德昂", "保安", "裕固", "京", "塔塔尔",
            "独龙", "鄂伦春", "赫哲", "门巴", "珞巴", "基诺"
    };
    private String name;
    private String sex;
    private String nation;
    private String birth;
    private String address;
    private String id;
    private String depart;
    private String validityTime;

    public IDCardInfo() {
    }

    public String getName() {
        return this.name;
    }

    private void setName(String var1) {
        this.name = var1;
    }

    public String getBirth() {
        return this.birth;
    }

    private void setBirth(String var1) {
        this.birth = var1;
    }

    public String getId() {
        return this.id;
    }

    private void setId(String var1) {
        this.id = var1;
    }

    public String getAddress() {
        return this.address;
    }

    private void setAddress(String var1) {
        this.address = var1;
    }

    public String getDepart() {
        return this.depart;
    }

    private void setDepart(String var1) {
        this.depart = var1;
    }

    public String getSex() {
        return this.sex;
    }

    private void setSex(String var1) {
        this.sex = var1;
    }

    public String getNation() {
        return this.nation;
    }

    private void setNation(String var1) {
        this.nation = var1;
    }

    public String getValidityTime() {
        return this.validityTime;
    }

    private void setValidityTime(String var1) {
        this.validityTime = var1;
    }

    private String getSex(String var1) {
        return var1.equals("1") ? "男" : "女";
    }

    private String getNation(String var1) {
        int var2;
        return (var2 = Integer.parseInt(var1)) == 97 ? "????" : (var2 == 98 ? "?????" : (var2 > 0 && var2 <= 56 ? NATION_ARRAY[var2] : ""));
    }

    private String getBirthDay(String var1) {
        return var1.substring(0, 4) + "??" + var1.substring(4, 6) + "??" + var1.substring(6, 8) + "??";
    }

    private String getValidityTime(String var1) {
        return var1.substring(0, 4) + "." + var1.substring(4, 6) + "." + var1.substring(6, 8) + "-" + var1.substring(8, 12) + "." + var1.substring(12, 14) + "." + var1.substring(14, 16);
    }

    public byte[] getPhoto() {
        return this.photo;
    }

    private void decodeIdCardBaseInfo(byte[] cardInfoByte) throws UnsupportedEncodingException {
        String cardInfo = new String(cardInfoByte, "UTF16-LE");
        cardInfo = new String(cardInfo.getBytes("UTF-8"));
        this.setName(cardInfo.substring(0, 15).trim());
        this.setSex(this.getSex(cardInfo.substring(15, 16)));
        this.setNation(this.getNation(cardInfo.substring(16, 18)));
        this.setBirth(this.getBirthDay(cardInfo.substring(18, 26)));
        this.setAddress(cardInfo.substring(26, 61).trim());
        this.setId(cardInfo.substring(61, 79).trim());
        this.setDepart(cardInfo.substring(79, 94).trim().trim());
        this.setValidityTime(this.getValidityTime(cardInfo.substring(94, 110)));
       LUtils.d(TAG,"OOOOOOOOOOO"+cardInfo.toString());
    }

    private void decodeIdCardPic(byte[] cardPicByte) {
        byte[] cardPicBmp = new byte[WLTService.imgLength];
        if (1 == WLTService.wlt2Bmp(cardPicByte, cardPicBmp)) {
            this.photo = cardPicBmp;
        } else {
            this.photo = null;
        }
    }

    public void decodeCardInfo(byte[] cardInfoByte, byte[] cardPicByte) throws UnsupportedEncodingException {
        decodeIdCardBaseInfo(cardInfoByte);
       LUtils.e("wcl","Pic: " + ByteUtil.bytesToHexString(cardPicByte));
        decodeIdCardPic(cardPicByte);
    }


    @Override
    public String toString() {
        return "IDCardInfo{" +
                "TAG='" + TAG + '\'' +
                ", photo=" + Arrays.toString(photo) +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", nation='" + nation + '\'' +
                ", birth='" + birth + '\'' +
                ", address='" + address + '\'' +
                ", id='" + id + '\'' +
                ", depart='" + depart + '\'' +
                ", validityTime='" + validityTime + '\'' +
                '}';
    }
}
