package com.example.shopbepoly.DTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

public class VietnamAddress {
    private String name;
    private String code;
    private Map<String, District> districts;

    public VietnamAddress() {
        districts = new HashMap<>();
    }

    public VietnamAddress(String name, String code) {
        this.name = name;
        this.code = code;
        this.districts = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, District> getDistricts() {
        return districts;
    }

    public void setDistricts(Map<String, District> districts) {
        this.districts = districts;
    }

    public static class District {
        private String name;
        private String code;
        private Map<String, Ward> wards;

        public District() {
            wards = new HashMap<>();
        }

        public District(String name, String code) {
            this.name = name;
            this.code = code;
            this.wards = new HashMap<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Map<String, Ward> getWards() {
            return wards;
        }

        public void setWards(Map<String, Ward> wards) {
            this.wards = wards;
        }
    }

    public static class Ward {
        private String name;
        private String code;

        public Ward() {
        }

        public Ward(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    // Class tạm để parse đúng cấu trúc JSON
    public static class ProvinceRaw {
        public String level1_id;
        public String name;
        public String type;
        public List<DistrictRaw> level2s;
    }
    public static class DistrictRaw {
        public String level2_id;
        public String name;
        public String type;
        public List<WardRaw> level3s;
    }
    public static class WardRaw {
        public String level3_id;
        public String name;
        public String type;
    }

    // Đọc dữ liệu các tỉnh thành từ file JSON và chuyển sang List<VietnamAddress>
    public static List<VietnamAddress> loadFromJson(Context context) {
        try {
            InputStream is = context.getAssets().open("vietnam_addresses.json");
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            Type listType = new TypeToken<List<ProvinceRaw>>(){}.getType();
            List<ProvinceRaw> provinceRaws = new Gson().fromJson(reader, listType);
            reader.close();
            is.close();
            // Chuyển đổi sang List<VietnamAddress>
            List<VietnamAddress> provinces = new ArrayList<>();
            for (ProvinceRaw p : provinceRaws) {
                VietnamAddress va = new VietnamAddress(p.name, p.level1_id);
                if (p.level2s != null) {
                    for (DistrictRaw d : p.level2s) {
                        District district = new District(d.name, d.level2_id);
                        if (d.level3s != null) {
                            for (WardRaw w : d.level3s) {
                                district.getWards().put(w.level3_id, new Ward(w.name, w.level3_id));
                            }
                        }
                        va.getDistricts().put(d.level2_id, district);
                    }
                }
                provinces.add(va);
            }
            return provinces;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Lấy danh sách quận/huyện theo tỉnh từ dữ liệu JSON
    public static List<District> getDistrictsByProvince(List<VietnamAddress> provinces, String provinceCode) {
        for (VietnamAddress province : provinces) {
            if (province != null && province.getCode() != null && province.getCode().equals(provinceCode)) {
                return new ArrayList<>(province.getDistricts().values());
            }
        }
        return new ArrayList<>();
    }

    // Lấy danh sách xã/phường theo quận/huyện từ dữ liệu JSON
    public static List<Ward> getWardsByDistrict(List<VietnamAddress> provinces, String districtCode) {
        for (VietnamAddress province : provinces) {
            for (District district : province.getDistricts().values()) {
                if (district.getCode().equals(districtCode)) {
                    return new ArrayList<>(district.getWards().values());
                }
            }
        }
        return new ArrayList<>();
    }
} 